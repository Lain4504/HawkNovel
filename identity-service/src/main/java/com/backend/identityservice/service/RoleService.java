package com.backend.identityservice.service;

import com.backend.identityservice.dto.request.RoleListRequest;
import com.backend.identityservice.dto.request.RoleRequest;
import com.backend.identityservice.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {

        RoleResponse create(RoleRequest request);

        RoleResponse update(String roleId, RoleRequest request);

        void addRoleToUser(String userId, RoleListRequest request);

        void delete(String role);

        RoleResponse getRole(String role);

        List<RoleResponse> getAll();

        void updateRoleFromUser(String userId, RoleListRequest request);

}
