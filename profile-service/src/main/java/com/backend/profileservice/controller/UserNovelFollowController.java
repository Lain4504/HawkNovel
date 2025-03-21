package com.backend.profileservice.controller;

import com.backend.dto.response.ApiResponse;
import com.backend.dto.response.PageResponse;
import com.backend.profileservice.dto.request.UserNovelFollowRequest;
import com.backend.profileservice.dto.response.NovelDetailsResponse;
import com.backend.profileservice.dto.response.UserNovelFollowResponse;
import com.backend.profileservice.service.UserNovelFollowService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-novel-follow")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserNovelFollowController {
    UserNovelFollowService userNovelFollowService;

    @PostMapping("/follow")
    ApiResponse<UserNovelFollowResponse> followNovel(@RequestBody UserNovelFollowRequest request) {
        return ApiResponse.<UserNovelFollowResponse>builder()
                .result(userNovelFollowService.followNovel(request))
                .build();
    }

    @PostMapping("/unfollow")
    ApiResponse<Void> unfollowNovel(@RequestBody UserNovelFollowRequest request) {
        userNovelFollowService.unfollowNovel(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/is-following")
    ApiResponse<Boolean> isFollowingNovel(@RequestBody UserNovelFollowRequest request) {
        return ApiResponse.<Boolean>builder()
                .result(userNovelFollowService.isFollowingNovel(request))
                .build();
    }

    @GetMapping("/get/followed-novels/{userId}")
    ApiResponse<PageResponse<NovelDetailsResponse>> getFollowingNovelDetails(
            @PathVariable("userId") String userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<NovelDetailsResponse>>builder()
                .result(userNovelFollowService.getFollowingNovelsWithDetails(userId, page, size))
                .build();
    }

}
