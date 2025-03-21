package com.backend.profileservice.controller;

import com.backend.dto.response.ApiResponse;
import com.backend.dto.response.PageResponse;
import com.backend.profileservice.dto.request.UserBookmarkRequest;
import com.backend.profileservice.dto.response.UserBookmarkResponse;
import com.backend.profileservice.service.UserBookmarkService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/user-bookmarks")
public class UserBookmarkController {
    UserBookmarkService userBookmarkService;

    @PostMapping("/save/{userId}")
    public ApiResponse<UserBookmarkResponse> saveUserBookmark(@PathVariable String userId, @RequestBody UserBookmarkRequest request) {
        return ApiResponse.<UserBookmarkResponse>builder()
                .result(userBookmarkService.saveUserBookmark(userId, request)).build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUserBookmark(@PathVariable String id) {
        userBookmarkService.deleteUserBookmark(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<PageResponse<UserBookmarkResponse>> getUserBookmarks(
            @PathVariable String userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        PageResponse<UserBookmarkResponse> response = userBookmarkService.getUserBookmarks(userId, page, size);
        return ApiResponse.<PageResponse<UserBookmarkResponse>>builder().result(response).build();
    }
}
