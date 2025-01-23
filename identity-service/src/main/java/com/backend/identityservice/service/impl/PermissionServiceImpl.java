package com.backend.identityservice.service.impl;

import com.backend.exception.AppException;
import com.backend.exception.ErrorCode;
import com.backend.identityservice.dto.request.PermissionListRequest;
import com.backend.identityservice.dto.request.PermissionRequest;
import com.backend.identityservice.dto.response.PermissionResponse;
import com.backend.identityservice.entity.Permission;
import com.backend.identityservice.mapper.PermissionMapper;
import com.backend.identityservice.repository.PermissionRepository;
import com.backend.identityservice.repository.RoleRepository;
import com.backend.identityservice.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;
    RoleRepository roleRepository;

    @Transactional
    @Override
    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    @Transactional
    @Override
    public PermissionResponse update(String permissionId, PermissionRequest request) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        permissionMapper.updatePermissionFromRequest(request, permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    @Override
    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    @Override
    public PermissionResponse get(String permissionId) {
        var permissionEntity = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        return permissionMapper.toPermissionResponse(permissionEntity);
    }

    @Override
    public void delete(String permissionId) {
        var permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        permission.setDeleted(true);
        permissionRepository.save(permission);
    }

    @Override
    public void addPermissionToRole(String roleId, PermissionListRequest request) {
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        var permissions = permissionRepository.findAllById(request.getPermissionName());

        if (permissions.size() != request.getPermissionName().size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
        }

        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
    }

    @Override
    public void updatePermissionToRole(String roleId, PermissionListRequest request) {
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        var permissions = permissionRepository.findAllById(request.getPermissionName());

        if (permissions.size() != request.getPermissionName().size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
        }
        var newPermissions = new HashSet<>(permissions);
        role.setPermissions(newPermissions);
        roleRepository.save(role);
    }
}
