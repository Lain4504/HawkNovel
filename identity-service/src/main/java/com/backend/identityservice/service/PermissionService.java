package com.backend.identityservice.service;

import com.backend.identityservice.dto.request.PermissionRequest;
import com.backend.identityservice.dto.response.PermissionResponse;
import com.backend.identityservice.entity.Permission;
import com.backend.identityservice.mapper.PermissionMapper;
import com.backend.identityservice.repository.PermissionRepository;
import com.backend.identityservice.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;
    private final RoleRepository roleRepository;

    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }
    public PermissionResponse update(String permissionId, PermissionRequest request) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        permissionMapper.updatePermissionFromRequest(request, permission);
        return permissionMapper.toPermissionResponse(permission);
    }
    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }
    public PermissionResponse get(String permissionId) {
        var permissionEntity = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        return permissionMapper.toPermissionResponse(permissionEntity);
    }
    public void delete(String permission) {
        permissionRepository.deleteById(permission);
    }

    public void addPermissionToRole(String roleId, String permissionId) {
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        var permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        role.getPermissions().add(permission);
        roleRepository.save(role);
    }
}
