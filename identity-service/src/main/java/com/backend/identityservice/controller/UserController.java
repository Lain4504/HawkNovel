package com.backend.identityservice.controller;

import com.backend.dto.request.PagingRequest;
import com.backend.dto.request.SortRequest;
import com.backend.dto.response.ApiResponse;
import com.backend.dto.response.PageResponse;
import com.backend.identityservice.dto.request.UserCreationRequest;
import com.backend.identityservice.dto.request.UserUpdateRoleRequest;
import com.backend.identityservice.dto.response.UserResponse;
import com.backend.identityservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;

    @PostMapping("/registration")
    ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .message("User created successfully")
                .build();
    }

    @GetMapping
    ApiResponse<PageResponse<UserResponse>> getUsers(
            @RequestParam(value = "pageNumber", required = false, defaultValue = "1") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sortField", required = false, defaultValue = "createdDate") String sortField,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder
    ) {
        PagingRequest pagingRequest = new PagingRequest(
                page,
                size,
                new SortRequest(sortField, sortOrder)
        );
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(userService.getAllUsers(pagingRequest))
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
        return ApiResponse.<UserResponse>builder().result(userService.getUserById(userId)).build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable("userId") String userId, @RequestBody UserUpdateRoleRequest request) {
        return ApiResponse.<UserResponse>builder().result(userService.updateUser(userId, request)).build();
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder().result(userService.getMyInfo()).build();
    }
}
