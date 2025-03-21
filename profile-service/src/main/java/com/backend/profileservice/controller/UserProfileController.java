package com.backend.profileservice.controller;

import com.backend.dto.response.ApiResponse;
import com.backend.profileservice.dto.request.UserProfileUpdateRequest;
import com.backend.profileservice.dto.response.UserProfileResponse;
import com.backend.profileservice.service.UserProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/users")
public class UserProfileController {
    UserProfileService userProfileService;

    @GetMapping("/get/{userId}")
    ApiResponse<UserProfileResponse> getUserProfile(@PathVariable String userId) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.getProfile(userId)).
                build();
    }

    @DeleteMapping("/{profileId}")
    ApiResponse<Void> deleteUserProfile(@PathVariable String profileId) {
        userProfileService.deleteProfile(profileId);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{profileId}")
    ApiResponse<UserProfileResponse> updateUserProfile(
            @PathVariable String profileId,
            @RequestPart("profile") UserProfileUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.updateProfile(profileId, request, imageFile)).build();
    }

    @GetMapping("/get-all")
    ApiResponse<List<UserProfileResponse>> getAllProfiles() {
        return ApiResponse.<List<UserProfileResponse>>builder().result(userProfileService.getAllProfiles()).build();
    }
}
