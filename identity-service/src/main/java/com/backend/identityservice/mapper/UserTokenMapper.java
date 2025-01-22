package com.backend.identityservice.mapper;

import com.backend.identityservice.dto.request.UserTokenRequest;
import com.backend.identityservice.dto.response.UserTokenResponse;
import com.backend.identityservice.entity.UserToken;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserTokenMapper {

     UserToken toUserToken(UserTokenRequest request);

     UserTokenResponse toUserTokenDto(UserToken userToken);

     void updateFromRequest(UserTokenRequest request, @MappingTarget UserToken userToken);
}
