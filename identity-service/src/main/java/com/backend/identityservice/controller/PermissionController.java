package com.backend.identityservice.controller;

import com.backend.dto.response.ApiResponse;
import com.backend.identityservice.dto.request.PermissionListRequest;
import com.backend.identityservice.dto.request.PermissionRequest;
import com.backend.identityservice.dto.response.PermissionResponse;
import com.backend.identityservice.service.impl.PermissionServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {
    PermissionServiceImpl permissionServiceImpl;

    @PostMapping
    ApiResponse<PermissionResponse> createPermission(@RequestBody PermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionServiceImpl.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<PermissionResponse>> getPermissions() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionServiceImpl.getAll())
                .build();
    }

    @DeleteMapping("/{permissionId}")
    ApiResponse<Void> deletePermission(@PathVariable("permissionId") String permissionId) {
        permissionServiceImpl.delete(permissionId);
        return ApiResponse.<Void>builder()
                .build();
    }
    @GetMapping("/{permissionId}")
    ApiResponse<PermissionResponse> getPermission(@PathVariable("permissionId") String permissionId) {
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionServiceImpl.get(permissionId))
                .build();
    }
    @PutMapping("/{permissionId}")
    ApiResponse<PermissionResponse> updatePermission(@PathVariable("permissionId") String permissionId, @RequestBody PermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionServiceImpl.update(permissionId, request))
                .build();
    }
    @PostMapping("/{roleId}/permission")
    ApiResponse<Void> addPermissionToRole(@PathVariable("roleId") String roleId, @RequestBody PermissionListRequest request) {
        permissionServiceImpl.addPermissionToRole(roleId, request);
        return ApiResponse.<Void>builder()
                .build();
    }
    @PutMapping("/{roleId}/permission")
    ApiResponse<Void> updatePermissionToRole(@PathVariable("roleId") String roleId, @RequestBody PermissionListRequest request) {
        permissionServiceImpl.updatePermissionToRole(roleId, request);
        return ApiResponse.<Void>builder()
                .build();
    }

}
