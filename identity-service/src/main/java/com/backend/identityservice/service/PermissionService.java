package com.backend.identityservice.service;

import com.backend.identityservice.dto.request.PermissionListRequest;
import com.backend.identityservice.dto.request.PermissionRequest;
import com.backend.identityservice.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {

    PermissionResponse create(PermissionRequest request);

    PermissionResponse get(String permissionId);

    PermissionResponse update(String permissionId, PermissionRequest request);

    void delete(String permissionId);

    List<PermissionResponse> getAll();

    void addPermissionToRole(String roleId, PermissionListRequest request);

    void updatePermissionToRole(String roleId, PermissionListRequest permissionId);
}
