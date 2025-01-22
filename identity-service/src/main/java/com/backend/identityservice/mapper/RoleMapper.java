package com.backend.identityservice.mapper;

import com.backend.identityservice.dto.request.RoleRequest;
import com.backend.identityservice.dto.response.RoleResponse;
import com.backend.identityservice.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
    @Mapping(target = "permissions", ignore = true)
    void updateRoleFromRequest(RoleRequest request, @MappingTarget Role role);
}
