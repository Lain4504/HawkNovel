package com.backend.identityservice.mapper;

import com.backend.identityservice.dto.response.UserTokenResponse;
import com.backend.identityservice.entity.UserToken;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserTokenMapper {

     UserTokenResponse toUserTokenResponse(UserToken userToken);

}
