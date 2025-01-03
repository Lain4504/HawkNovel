package com.backend.profileservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.backend.profileservice.dto.request.ProfileCreationRequest;
import com.backend.profileservice.dto.request.UserProfileUpdateRequest;
import com.backend.profileservice.dto.response.UserProfileResponse;
import com.backend.profileservice.entity.UserProfile;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfile toUserProfile(ProfileCreationRequest request);

    UserProfileResponse toUserProfileResponse(UserProfile userProfile);

    void updateUserProfile(@MappingTarget UserProfile user, UserProfileUpdateRequest request);
}
