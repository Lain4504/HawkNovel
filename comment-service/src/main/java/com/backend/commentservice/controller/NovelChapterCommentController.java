package com.backend.commentservice.controller;

import com.backend.commentservice.dto.request.NovelChapterCommentReplyRequest;
import com.backend.commentservice.dto.request.NovelChapterCommentRequest;
import com.backend.commentservice.dto.response.NovelChapterCommentReplyResponse;
import com.backend.commentservice.dto.response.NovelChapterCommentResponse;
import com.backend.commentservice.service.NovelChapterCommentService;
import com.backend.dto.response.ApiResponse;
import com.backend.dto.response.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/novel-chapter-comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NovelChapterCommentController {
    NovelChapterCommentService novelChapterCommentService;

    @GetMapping("/get/{chapterId}")
    public ApiResponse<PageResponse<NovelChapterCommentResponse>> getAllComments(
            @PathVariable String chapterId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        return ApiResponse.<PageResponse<NovelChapterCommentResponse>>builder()
                .result(novelChapterCommentService.getAllComments(chapterId, page, size))
                .build();
    }

    @PostMapping
    public ApiResponse<NovelChapterCommentResponse> createComment(@Valid @RequestBody NovelChapterCommentRequest request) {
        return ApiResponse.<NovelChapterCommentResponse>builder()
                .result(novelChapterCommentService.createComment(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<NovelChapterCommentResponse> updateComment(@PathVariable String id, @Valid @RequestBody NovelChapterCommentRequest request) {
        return ApiResponse.<NovelChapterCommentResponse>builder()
                .result(novelChapterCommentService.updateComment(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable String id) {
        novelChapterCommentService.deleteComment(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/replies")
    public ApiResponse<NovelChapterCommentReplyResponse> createReply(@Valid @RequestBody NovelChapterCommentReplyRequest request) {
        return ApiResponse.<NovelChapterCommentReplyResponse>builder()
                .result(novelChapterCommentService.createReply(request))
                .build();
    }

    @PutMapping("/replies/{id}")
    public ApiResponse<NovelChapterCommentReplyResponse> updateReply(@PathVariable String id, @Valid @RequestBody NovelChapterCommentReplyRequest request) {
        return ApiResponse.<NovelChapterCommentReplyResponse>builder()
                .result(novelChapterCommentService.updateReply(id, request))
                .build();
    }

    @DeleteMapping("/replies/{id}")
    public ApiResponse<Void> deleteReply(@PathVariable String id) {
        novelChapterCommentService.deleteReply(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/replies/get/{commentId}")
    public ApiResponse<PageResponse<NovelChapterCommentReplyResponse>> getAllRepliesByCommentId(
            @PathVariable String commentId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        return ApiResponse.<PageResponse<NovelChapterCommentReplyResponse>>builder()
                .result(novelChapterCommentService.getAllRepliesByCommentId(commentId, page, size))
                .build();
    }
}
