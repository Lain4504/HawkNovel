package com.backend.profileservice.mapper;

import com.backend.profileservice.dto.request.UserNovelRatingRequest;
import com.backend.profileservice.dto.response.UserNovelRatingResponse;
import com.backend.profileservice.entity.UserNovelRating;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserNovelRatingMapper {
    UserNovelRating toUserNovelRating(UserNovelRatingRequest request);

    UserNovelRatingResponse toUserNovelRatingResponse(UserNovelRating userNovelRating);

    void updateUserNovelRating(@MappingTarget UserNovelRating userNovelRating, UserNovelRatingRequest request);
}
