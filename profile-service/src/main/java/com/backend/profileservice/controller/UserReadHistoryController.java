package com.backend.profileservice.controller;

import java.util.Optional;

import org.springframework.web.bind.annotation.*;

import com.backend.dto.response.ApiResponse;
import com.backend.dto.response.PageResponse;
import com.backend.profileservice.dto.request.UserReadHistoryRequest;
import com.backend.profileservice.dto.response.NovelDetailsResponse;
import com.backend.profileservice.dto.response.UserReadHistoryResponse;
import com.backend.profileservice.service.UserReadHistoryService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/user-read-history")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserReadHistoryController {
    UserReadHistoryService userReadHistoryService;

    @PostMapping
    public ApiResponse<UserReadHistoryResponse> addReadHistory(@RequestBody UserReadHistoryRequest userReadHistory) {
        return ApiResponse.<UserReadHistoryResponse>builder()
                .result(userReadHistoryService.createOrUpdateHistory(userReadHistory))
                .build();
    }

    @DeleteMapping("/{novelId}")
    public ApiResponse<Void> deleteReadHistory(@PathVariable String novelId) {
        userReadHistoryService.delete(novelId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/has-read/{novelId}")
    public ApiResponse<Optional> hasReadNovel(@PathVariable String novelId) {
        return ApiResponse.<Optional>builder()
                .result(userReadHistoryService.getById(novelId))
                .build();
    }

    @GetMapping("/reading-history/{userId}")
    public ApiResponse<PageResponse<NovelDetailsResponse>> getReadingHistory(
            @PathVariable String userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<NovelDetailsResponse>>builder()
                .result(userReadHistoryService.getReadHistoryWithDetails(userId, page, size))
                .build();
    }
}
