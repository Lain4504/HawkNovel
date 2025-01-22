package com.backend.identityservice.service;

import com.backend.event.NotificationEvent;
import com.backend.exception.AppException;
import com.backend.exception.ErrorCode;
import com.backend.identityservice.constant.PredefinedRole;
import com.backend.identityservice.dto.request.*;
import com.backend.identityservice.dto.response.AuthenticationResponse;
import com.backend.identityservice.dto.response.IntrospectResponse;
import com.backend.identityservice.entity.Role;
import com.backend.identityservice.entity.User;
import com.backend.identityservice.enums.UserState;
import com.backend.identityservice.repository.UserRepository;
import com.backend.identityservice.repository.httpclient.OutboundIdentityClient;
import com.backend.identityservice.repository.httpclient.OutboundUserClient;
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
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";
    UserRepository userRepository;
    OutboundIdentityClient outboundIdentityClient;
    RefreshTokenRepository refreshTokenRepository;
    OutboundUserClient outboundUserClient;
    InvalidatedTokenRepository invalidatedTokenRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
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

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

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
                        .username(UUID.randomUUID().toString())
                        .roles(roles)
                        .build()));
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.INCORRECT_CREDENTIALS);
        boolean isActivated = user.getState().equals(UserState.ACTIVE);
        if (!isActivated) throw new AppException(ErrorCode.INACTIVE_USER);
        var accessToken = generateToken(user);
        var refreshToken = generateRefreshToken(user);
        return AuthenticationResponse.builder().token(accessToken).refreshToken(refreshToken).authenticated(true).build();
    }

    private String generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(REFRESHABLE_DURATION))
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getAccessToken());
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();
            invalidatedTokenRepository.save(invalidatedToken);
            var refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken()).orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));
            refreshTokenRepository.delete(refreshToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String userEmailFromAccessToken = authentication.getName(); // Lấy userId từ authentication
        RefreshToken existingRefreshToken = refreshTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        if (!existingRefreshToken.getUser().getEmail().equals(userEmailFromAccessToken)) {
            throw new RuntimeException("User mismatch: This refresh token doesn't belong to the current user");
        }
        if (existingRefreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }
        var user = userRepository.findById(existingRefreshToken.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String newAccessToken = generateToken(user);
        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(existingRefreshToken.getToken())
                .authenticated(true)
                .build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("lain4504.com")
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
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date()))) throw new RuntimeException("Token invalid");
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new RuntimeException("Token already invalidated");
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
        return generateToken(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
    }

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
