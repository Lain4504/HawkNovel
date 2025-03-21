package com.backend.profileservice.mapper;

import com.backend.profileservice.dto.request.UserNovelFollowRequest;
import com.backend.profileservice.dto.response.UserNovelFollowResponse;
import com.backend.profileservice.entity.UserNovelFollow;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserNovelFollowMapper {
    UserNovelFollow toUserNovelFollow(UserNovelFollowRequest request);

    UserNovelFollowResponse toUserNovelFollowResponse(UserNovelFollow userNovelFollow);

    void updateUserNovelFollow(@MappingTarget UserNovelFollow userNovelFollow, UserNovelFollowRequest request);
}

