package com.backend.identityservice.service;

import com.backend.dto.request.PagingRequest;
import com.backend.dto.response.PageResponse;
import com.backend.identityservice.dto.request.UserCreationRequest;
import com.backend.identityservice.dto.request.UserUpdateRoleRequest;
import com.backend.identityservice.dto.response.UserResponse;


public interface UserService {

    UserResponse createUser(UserCreationRequest request);

    UserResponse updateUser(String userId, UserUpdateRoleRequest request);

    UserResponse getMyInfo();

    PageResponse<UserResponse> getAllUsers(PagingRequest request);

    UserResponse getUserById(String userId);
}
