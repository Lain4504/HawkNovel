package com.backend.identityservice.service.impl;

import com.backend.event.NotificationEvent;
import com.backend.exception.AppException;
import com.backend.exception.ErrorCode;
import com.backend.identityservice.constant.PredefinedRole;
import com.backend.identityservice.dto.request.*;
import com.backend.identityservice.dto.response.AuthenticationResponse;
import com.backend.identityservice.dto.response.IntrospectResponse;
import com.backend.identityservice.dto.response.UserTokenResponse;
import com.backend.identityservice.entity.Role;
import com.backend.identityservice.entity.User;
import com.backend.identityservice.entity.UserToken;
import com.backend.identityservice.enums.UserState;
import com.backend.identityservice.mapper.UserTokenMapper;
import com.backend.identityservice.repository.UserRepository;
import com.backend.identityservice.repository.UserTokenRepository;
import com.backend.identityservice.repository.httpclient.OutboundIdentityClient;
import com.backend.identityservice.repository.httpclient.OutboundUserClient;
import com.backend.identityservice.service.AuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";
    UserRepository userRepository;
    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;
    KafkaTemplate<String, Object> kafkaTemplate;
    UserTokenRepository userTokenRepository;
    private final UserTokenMapper userTokenMapper;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;
    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;
    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;
    @NonFinal
    @Value("${outbound.identity.client-id}")
    protected String CLIENT_ID;
    @NonFinal
    @Value("${outbound.identity.client-secret}")
    protected String CLIENT_SECRET;
    @NonFinal
    @Value("${outbound.identity.redirect-uri}")
    protected String REDIRECT_URI;

    private final static String ISSUER = "lain4504.com";

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getAccessToken();
        boolean isValid = true;
        try {
            verifyToken(token);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    @Override
    public AuthenticationResponse outboundAuthenticate(String code) {
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());
        log.info("TOKEN RESPONSE {}", response);
        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());
        log.info("User Info {}", userInfo);
        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().name(PredefinedRole.USER_ROLE).build());
        var user = userRepository.findByEmail(userInfo.getEmail()).orElseGet(
                () -> userRepository.save(User.builder()
                        .email(userInfo.getEmail())
                        .roles(roles)
                        .build()));
        var userToken = generateToken(user);
        return AuthenticationResponse.builder()
                .accessToken(userToken.getAccessToken()).build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.INCORRECT_CREDENTIALS);
        boolean isActivated = user.getState().equals(UserState.ACTIVE);
        if (!isActivated) throw new AppException(ErrorCode.INACTIVE_USER);
        var userToken = generateToken(user);
        return AuthenticationResponse.builder()
                .accessToken(userToken.getAccessToken())
                .refreshToken(userToken.getRefreshToken())
                .authenticated(true)
                .build();
    }

    @Override
    public void logout() throws ParseException, JOSEException {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();
        var jwt = (org.springframework.security.oauth2.jwt.Jwt) context.getAuthentication().getCredentials();
        String accessToken = jwt.getTokenValue();
        try {
            verifyToken(accessToken);
            var userToken = userTokenRepository.findByUserIdAndAccessToken(userId, accessToken).orElseThrow(() ->
                    new AppException(ErrorCode.DATA_NOT_FOUND));
            userTokenRepository.deleteById(userToken.getId());
        } catch (RuntimeException exception) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    @Override
    public AuthenticationResponse refreshAccessToken() throws ParseException, JOSEException {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();
        var jwt = (org.springframework.security.oauth2.jwt.Jwt) context.getAuthentication().getCredentials();
        var accessToken = jwt.getTokenValue();
        if (!userTokenRepository.existsByUserId(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        UserToken userToken = userTokenRepository.findByUserIdAndAccessToken(userId, accessToken)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
        if (!userToken.getUserId().equals(userId)) {
            throw new RuntimeException("User mismatch: This refresh token doesn't belong to the current user");
        }
        LocalDateTime expirationDate = getExpirationDateFromToken(userToken.getRefreshToken());
        if (expirationDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String newAccessToken = generateAccessToken(user);
        userToken.setAccessToken(newAccessToken);
        userToken.setExpiryTime(LocalDateTime.now().plusSeconds(VALID_DURATION));
        userTokenRepository.save(userToken);
        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(userToken.getRefreshToken())
                .authenticated(true)
                .build();
    }

    private LocalDateTime getExpirationDateFromToken(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
        return expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private UserTokenResponse generateToken(User user) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        UserToken userToken = UserToken.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiryTime(LocalDateTime.now().plusSeconds(VALID_DURATION))
                .build();

        userTokenRepository.save(userToken);

        return userTokenMapper.toUserTokenResponse(userToken);
    }

    private String generateAccessToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer(ISSUER)
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create access token", e);
            throw new RuntimeException(e);
        }
    }

    private String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer(ISSUER)
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create refresh token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new RuntimeException("Invalid token");
        }
        // Check if the token exists in the database
        if (!userTokenRepository.existsByAccessToken(token)) {
            throw new RuntimeException("Token not found in database");
        }

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });
        return stringJoiner.toString();
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) throws ParseException, JOSEException {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password not match");
        }
        var signedJWT = verifyToken(request.getToken());
        String id = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String id = authentication.getName();
        var user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        var token = generateToken(user);
        var subject = "Reset password";
        NotificationEvent event = NotificationEvent
                .builder()
                .channel("EMAIL")
                .recipient(user.getEmail())
                .templateCode("RESET_PASSWORD")
                .param(Map.of("token", token, "subject", subject))
                .build();
        kafkaTemplate.send("reset-password-request", event);
    }

    public String activeAccountCode(String userId) {
        return generateAccessToken(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
    }

    @Override
    public void activeAccount(ActivationTokenRequest request) {
        try {
            var signedJWT = verifyToken(request.getToken());
            String id = signedJWT.getJWTClaimsSet().getSubject();
            var user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setState(UserState.ACTIVE);
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }
}
