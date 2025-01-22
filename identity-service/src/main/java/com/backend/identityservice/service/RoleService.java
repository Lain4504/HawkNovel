package com.backend.identityservice.service;

import com.backend.identityservice.dto.request.RoleRequest;
import com.backend.identityservice.dto.response.RoleResponse;
import com.backend.identityservice.mapper.RoleMapper;
import com.backend.identityservice.repository.PermissionRepository;
import com.backend.identityservice.repository.RoleRepository;
import com.backend.identityservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;
    UserRepository userRepository;

    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public RoleResponse update(String roleId, RoleRequest request) {
        var role = roleRepository.findById(roleId).orElseThrow();
        roleMapper.updateRoleFromRequest(request, role);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }
    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    public void delete(String role) {
        roleRepository.deleteById(role);
    }

    public RoleResponse getRole(String role) {
        return roleRepository.findById(role).map(roleMapper::toRoleResponse).orElse(null);
    }
    public void addRoleToUser(String userId, String roleId) {
        var user = userRepository.findById(userId).orElseThrow();
        var role = roleRepository.findById(roleId).orElseThrow();
        user.getRoles().add(role);
        userRepository.save(user);
    }

}
