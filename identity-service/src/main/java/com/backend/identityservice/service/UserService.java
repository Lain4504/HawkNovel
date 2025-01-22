package com.backend.identityservice.service;

import com.backend.dto.request.PagingRequest;
import com.backend.dto.response.PageResponse;
import com.backend.event.NotificationEvent;
import com.backend.exception.AppException;
import com.backend.exception.ErrorCode;
import com.backend.identityservice.constant.PredefinedRole;
import com.backend.identityservice.dto.request.UserCreationRequest;
import com.backend.identityservice.dto.request.UserUpdateRequest;
import com.backend.identityservice.dto.response.UserResponse;
import com.backend.identityservice.entity.Role;
import com.backend.identityservice.entity.User;
import com.backend.identityservice.enums.UserState;
import com.backend.identityservice.mapper.ProfileMapper;
import com.backend.identityservice.mapper.UserMapper;
import com.backend.identityservice.repository.RoleRepository;
import com.backend.identityservice.repository.UserRepository;
import com.backend.identityservice.repository.httpclient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    ProfileClient profileClient;
    ProfileMapper profileMapper;
    KafkaTemplate<String, Object> kafkaTemplate;
    AuthenticationService authenticationService;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) throw new AppException(ErrorCode.USER_EXISTED);
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        user.setState(UserState.INACTIVE);
        user.setCreatedAt(java.time.LocalDate.now());
        user = userRepository.save(user);
        var profileRequest = profileMapper.toProfileCreationRequest(request);
        profileRequest.setUserId(user.getId());
        profileClient.createProfile(profileRequest);
        var activeToken = authenticationService.activeAccountCode(user.getId());
        var subject = "Activate your account";
        NotificationEvent event = NotificationEvent
                .builder()
                .channel("EMAIL")
                .recipient(user.getEmail())
                .templateCode("ACTIVATION_EMAIL")
                .param(Map.of("activeToken", activeToken, "subject", subject))
                .build();
        //Publish message to kafka
        kafkaTemplate.send("onboard-successfully", event);
        return userMapper.toUserResponse(user);
    }

    @PostAuthorize("returnObject.email == authentication.name")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new MessageDescriptorFormatException("User not found"));
        userMapper.updateUser(user, request);
        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String id = context.getAuthentication().getName();
        User user = userRepository.findById(id).orElseThrow(() -> new MessageDescriptorFormatException("User not found"));
        return userMapper.toUserResponse(user);
    }

    public PageResponse<UserResponse> getAllUsers(PagingRequest pagingRequest) {
        Sort sort = Sort.by(Sort.Order.desc(pagingRequest.getSort().getField()));
        var pageData = userRepository.findAll(PageRequest.of(pagingRequest.getCurrentPage() - 1, pagingRequest.getPageSize(), sort));
        var userList = pageData.getContent().stream().map(userMapper::toUserResponse).collect(Collectors.toList());
        return PageResponse.<UserResponse>builder()
                .paging(pagingRequest)
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(userList)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(String userId) {
        return userMapper.toUserResponse(userRepository.findById(userId).orElseThrow(() -> new MessageDescriptorFormatException("User not found")));
    }

}
