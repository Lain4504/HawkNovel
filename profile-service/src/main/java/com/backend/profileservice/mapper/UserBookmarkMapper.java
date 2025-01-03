package com.backend.profileservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.backend.profileservice.dto.request.UserBookmarkRequest;
import com.backend.profileservice.dto.response.UserBookmarkResponse;
import com.backend.profileservice.entity.UserBookmark;

@Mapper(componentModel = "spring")
public interface UserBookmarkMapper {
    UserBookmark toUserBookmark(UserBookmarkRequest request);

    UserBookmarkResponse toUserBookmarkResponse(UserBookmark userBookmark);

    void updateUserReadHistory(@MappingTarget UserBookmark userBookmark, UserBookmarkRequest request);
}
