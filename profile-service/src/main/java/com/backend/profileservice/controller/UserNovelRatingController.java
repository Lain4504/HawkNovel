package com.backend.profileservice.controller;

import org.springframework.web.bind.annotation.*;

import com.backend.dto.response.ApiResponse;
import com.backend.profileservice.dto.request.UserNovelRatingRequest;
import com.backend.profileservice.dto.response.UserNovelRatingResponse;
import com.backend.profileservice.service.UserNovelRatingService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/user-novel-rating")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserNovelRatingController {
    UserNovelRatingService userNovelRatingService;

    @PostMapping("/rate")
    public ApiResponse<UserNovelRatingResponse> rateNovel(@RequestBody UserNovelRatingRequest request) {
        return ApiResponse.<UserNovelRatingResponse>builder()
                .result(userNovelRatingService.rateNovel(request))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<UserNovelRatingResponse> updateRating(@RequestBody UserNovelRatingRequest request) {
        return ApiResponse.<UserNovelRatingResponse>builder()
                .result(userNovelRatingService.updateRating(request))
                .build();
    }

    @GetMapping("/has-rated/{userId}/{novelId}")
    public ApiResponse<Boolean> hasRatedNovel(@PathVariable String userId, @PathVariable String novelId) {
        return ApiResponse.<Boolean>builder()
                .result(userNovelRatingService.hasRatedNovel(userId, novelId))
                .build();
    }

    @GetMapping("/get-rating/{userId}/{novelId}")
    public ApiResponse<UserNovelRatingResponse> getRating(@PathVariable String userId, @PathVariable String novelId) {
        return ApiResponse.<UserNovelRatingResponse>builder()
                .result(userNovelRatingService.getRating(userId, novelId))
                .build();
    }
}
