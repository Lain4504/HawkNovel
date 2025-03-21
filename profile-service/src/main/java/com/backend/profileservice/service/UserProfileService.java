package com.backend.profileservice.service;

import com.backend.profileservice.dto.request.ProfileCreationRequest;
import com.backend.profileservice.dto.request.UserProfileUpdateRequest;
import com.backend.profileservice.dto.response.UserProfileResponse;
import com.backend.profileservice.entity.Image;
import com.backend.profileservice.entity.UserProfile;
import com.backend.profileservice.mapper.UserProfileMapper;
import com.backend.profileservice.repository.UserProfileRepository;
import com.backend.utils.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileService {
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;
    ImageService imageService;
    DateTimeFormatter dateTimeFormatter;

    public UserProfileResponse createProfile(ProfileCreationRequest request) {
        UserProfile userProfile = userProfileMapper.toUserProfile(request);
        userProfile.setCreatedAt(Instant.now());
        userProfile = userProfileRepository.save(userProfile);
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public UserProfileResponse getProfile(String userId) {
        UserProfile userProfile =
                userProfileRepository.findByUserId((userId)).orElseThrow(() -> new RuntimeException("Profile not found"));
        UserProfileResponse response = userProfileMapper.toUserProfileResponse(userProfile);
        response.setCreated(dateTimeFormatter.format(userProfile.getCreatedAt()));
        return response;
    }

    public void deleteProfile(String id) {
        userProfileRepository.deleteById(id);
    }

    public UserProfileResponse updateProfile(String id, UserProfileUpdateRequest request, MultipartFile imageFile) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        userProfileMapper.updateUserProfile(userProfile, request);

        if (imageFile != null && !imageFile.isEmpty()) {
            if (userProfile.getImage() != null && !userProfile.getImage().getPath().equals(request.getImageUrl())) {
                imageService.deleteImage(userProfile.getImage().getId());
            }
            Image newImage = imageService.uploadImage(imageFile);
            userProfile.setImage(newImage);
        } else if (userProfile.getImage() == null || !userProfile.getImage().getPath().equals(request.getImageUrl())) {
            if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
                Image newImage = new Image();
                newImage.setPath(request.getImageUrl());
                userProfile.setImage(newImage);
            }
        }

        userProfile = userProfileRepository.save(userProfile);
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public List<UserProfileResponse> getAllProfiles() {
        var userProfiles = userProfileRepository.findAll();
        return userProfiles.stream()
                .map(userProfileMapper::toUserProfileResponse)
                .toList();
    }
}
