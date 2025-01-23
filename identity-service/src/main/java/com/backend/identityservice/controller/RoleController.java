package com.backend.identityservice.controller;

import com.backend.dto.response.ApiResponse;
import com.backend.identityservice.dto.request.RoleListRequest;
import com.backend.identityservice.dto.request.RoleRequest;
import com.backend.identityservice.dto.response.RoleResponse;
import com.backend.identityservice.service.impl.RoleServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleServiceImpl roleServiceImpl;

    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleServiceImpl.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder().result(roleServiceImpl.getAll()).build();
    }

    @DeleteMapping("/{role}")
    ApiResponse<Void> delete(@PathVariable String role) {
        roleServiceImpl.delete(role);
        return ApiResponse.<Void>builder().build();
    }
    @GetMapping("/{role}")
    ApiResponse<RoleResponse> getRole(@PathVariable String role) {
        return ApiResponse.<RoleResponse>builder().result(roleServiceImpl.getRole(role)).build();
    }
    @PostMapping("/{roleId}")
    ApiResponse<RoleResponse> update(@PathVariable String roleId, @RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder().result(roleServiceImpl.update(roleId, request)).build();
    }
    @PostMapping("/{userId}/role")
    ApiResponse<Void> addRoleToUser(@PathVariable String userId, @PathVariable RoleListRequest request) {
        roleServiceImpl.addRoleToUser(userId, request);
        return ApiResponse.<Void>builder().build();
    }
    @DeleteMapping("/{userId}/role")
    ApiResponse<Void> updateRoleFromUser(@PathVariable String userId, @PathVariable RoleListRequest request) {
        roleServiceImpl.updateRoleFromUser(userId, request);
        return ApiResponse.<Void>builder().build();
    }
}
