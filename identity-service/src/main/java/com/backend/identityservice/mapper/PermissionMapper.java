package com.backend.identityservice.mapper;
import com.backend.identityservice.dto.request.PermissionRequest;
import com.backend.identityservice.dto.response.PermissionResponse;
import com.backend.identityservice.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
