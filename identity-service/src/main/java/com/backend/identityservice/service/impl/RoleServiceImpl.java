package com.backend.identityservice.service.impl;

import com.backend.identityservice.dto.request.RoleListRequest;
import com.backend.identityservice.dto.request.RoleRequest;
import com.backend.identityservice.dto.response.RoleResponse;
import com.backend.identityservice.mapper.RoleMapper;
import com.backend.identityservice.repository.PermissionRepository;
import com.backend.identityservice.repository.RoleRepository;
import com.backend.identityservice.repository.UserRepository;
import com.backend.identityservice.service.RoleService;
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
public class RoleServiceImpl implements RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;
    UserRepository userRepository;

    @Override
    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    @Override
    public RoleResponse update(String roleId, RoleRequest request) {
        var role = roleRepository.findById(roleId).orElseThrow();
        roleMapper.updateRoleFromRequest(request, role);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }
    @Override
    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    @Override
    public void delete(String role) {
        roleRepository.deleteById(role);
    }

    @Override
    public RoleResponse getRole(String role) {
        return roleRepository.findById(role).map(roleMapper::toRoleResponse).orElse(null);
    }

    @Override
    public void addRoleToUser(String userId, RoleListRequest request) {
        var user = userRepository.findById(userId).orElseThrow();
        var roles = roleRepository.findAllById(request.getRoleName());
        user.getRoles().addAll(roles);
        userRepository.save(user);
    }
    @Override
    public void updateRoleFromUser(String userId, RoleListRequest request) {
        var user = userRepository.findById(userId).orElseThrow();
        var roles = roleRepository.findAllById(request.getRoleName());
        user.getRoles().removeAll(roles);
        userRepository.save(user);
    }

}
