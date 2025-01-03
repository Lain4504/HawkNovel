package com.backend.profileservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.backend.profileservice.dto.request.UserReviewRequest;
import com.backend.profileservice.dto.response.UserReviewResponse;
import com.backend.profileservice.entity.UserReview;

@Mapper(componentModel = "spring")
public interface UserReviewMapper {
    UserReview toUserReview(UserReviewRequest request);

    UserReviewResponse toUserReviewResponse(UserReview userReview);

    void updateUserReview(@MappingTarget UserReview userReview, UserReviewRequest request);
}
