package com.backend.commentservice.service;

import com.backend.commentservice.dto.request.PostCommentReplyRequest;
import com.backend.commentservice.dto.request.PostCommentRequest;
import com.backend.commentservice.dto.response.PostCommentReplyResponse;
import com.backend.commentservice.dto.response.PostCommentResponse;
import com.backend.commentservice.entity.PostComment;
import com.backend.commentservice.entity.PostCommentReply;
import com.backend.commentservice.mapper.PostCommentMapper;
import com.backend.commentservice.mapper.PostCommentReplyMapper;
import com.backend.commentservice.repository.PostCommentReplyRepository;
import com.backend.commentservice.repository.PostCommentRepository;
import com.backend.commentservice.repository.httpclient.UserProfileClient;
import com.backend.commentservice.repository.httpclient.UserProfileResponse;
import com.backend.dto.response.PageResponse;
import com.backend.event.NotificationEvent;
import com.backend.utils.DateTimeFormatterUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostCommentService {
    PostCommentReplyRepository postCommentReplyRepository;
    PostCommentRepository postCommentRepository;
    PostCommentMapper postCommentMapper;
    PostCommentReplyMapper postCommentReplyMapper;
    UserProfileClient userProfileClient;
    KafkaTemplate<String, Object> kafkaTemplate;
    DateTimeFormatterUtils dateTimeFormatter;

    public PageResponse<PostCommentResponse> getAllComments(String postId, int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("createdDate"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = postCommentRepository.findAllByPostId(postId, pageable);
        var commentList = pageData.getContent().stream()
                .map(this::enrichCommentWithUserProfile)
                .map(postComment -> {
                    PostCommentResponse response = postCommentMapper.toPostCommentResponse(postComment);
                    response.setCreated(dateTimeFormatter.format(postComment.getCreatedDate()));
                    return response;
                })
                .collect(Collectors.toList());
        return PageResponse.<PostCommentResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(commentList)
                .build();
    }

    public PostCommentResponse createComment(PostCommentRequest request) {
        PostComment postComment = postCommentMapper.toPostComment(request);
        postComment.setCreatedDate(Instant.now());
        postComment.setUpdateDateTime(Instant.now());
        UserProfileResponse userProfile = userProfileClient.getUserProfile(postComment.getUserId());
        postComment.setUsername(userProfile.getUsername());
        if (!postComment.getOwnerId().equals(postComment.getUserId())) {
            NotificationEvent event = NotificationEvent
                    .builder()
                    .channel("POST")
                    .recipient(postComment.getOwnerId())
                    .templateCode("POST_COMMENT_OWNER_NOTIFICATION")
                    .param(Map.of("fromUser", postComment.getUsername(), "inLocation", postComment.getPostName(),
                            "content", postComment.getContent()))
                    .build();
            //Publish message to kafka
            kafkaTemplate.send("comment-notification", event);
        }
        return postCommentMapper.toPostCommentResponse(postCommentRepository.save(postComment));
    }

    public PostCommentResponse updateComment(String id, PostCommentRequest request) {
        return postCommentRepository.findById(id)
                .map(existingComment -> {
                    postCommentMapper.updatePostComment(existingComment, request);
                    existingComment.setUpdateDateTime(Instant.now());
                    return postCommentMapper.toPostCommentResponse(postCommentRepository.save(existingComment));
                })
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    public void deleteComment(String id) {
        postCommentRepository.deleteById(id);
    }

    public PageResponse<PostCommentReplyResponse> getAllRepliesByCommentId(String commentId, int page, int size) {
        Sort sort = Sort.by(Sort.Order.asc("createdDate"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = postCommentReplyRepository.findAllByCommentId(commentId, pageable);
        var replyList = pageData.getContent().stream()
                .map(this::enrichReplyWithUserProfile)
                .map(reply -> {
                    PostCommentReplyResponse response = postCommentReplyMapper.toPostCommentReplyResponse(reply);
                    response.setCreated(dateTimeFormatter.format(reply.getCreatedDate()));
                    return response;
                }).collect(Collectors.toList());
        return PageResponse.<PostCommentReplyResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(replyList)
                .build();
    }

    public PostCommentReplyResponse createReply(PostCommentReplyRequest request) {
        PostCommentReply postCommentReply = postCommentReplyMapper.toPostCommentReply(request);
        postCommentReply.setCreatedDate(Instant.now());
        postCommentReply.setUpdateDateTime(Instant.now());
        postCommentReply.setUsername(userProfileClient.getUserProfile(postCommentReply.getUserId()).getUsername());
        postCommentRepository.findById(postCommentReply.getCommentId())
                .ifPresent(comment -> {
                    comment.setReplyCount(comment.getReplyCount() + 1);
                    postCommentRepository.save(comment);
                });
        if (!postCommentReply.getUserIdOfReplyTo().equals(postCommentReply.getUserId())) {
            NotificationEvent event = NotificationEvent
                    .builder()
                    .channel("POST")
                    .recipient(postCommentReply.getUserIdOfReplyTo())
                    .templateCode("POST_COMMENT_REPLY_NOTIFICATION")
                    .param(Map.of("fromUser", postCommentReply.getUsername(), "inLocation", postCommentReply.getPostName(),
                            "content", postCommentReply.getReplyContent()))
                    .build();
            //Publish message to kafka
            kafkaTemplate.send("comment-notification", event);
        }
        return postCommentReplyMapper.toPostCommentReplyResponse(postCommentReplyRepository.save(postCommentReply));
    }

    public PostCommentReplyResponse updateReply(String id, PostCommentReplyRequest request) {
        return postCommentReplyRepository.findById(id)
                .map(existingReply -> {
                    postCommentReplyMapper.updatePostCommentReplyFromRequest(existingReply, request);
                    existingReply.setUpdateDateTime(Instant.now());
                    return postCommentReplyMapper.toPostCommentReplyResponse(postCommentReplyRepository.save(existingReply));
                })
                .orElseThrow(() -> new RuntimeException("Reply not found"));
    }

    public void deleteReply(String id) {
        postCommentReplyRepository.findById(id)
                .ifPresent(reply -> {
                    postCommentRepository.findById(reply.getCommentId())
                            .ifPresent(comment -> {
                                comment.setReplyCount(comment.getReplyCount() - 1);
                                postCommentRepository.save(comment);
                            });
                });
        postCommentReplyRepository.deleteById(id);
    }

    PostComment enrichCommentWithUserProfile(PostComment comment) {
        UserProfileResponse userProfile = userProfileClient.getUserProfile(comment.getUserId());
        comment.setUsername(userProfile.getUsername());
        if (userProfile.getImage() != null) {
            comment.setUserAvatar(userProfile.getImage().getPath());
        } else {
            comment.setUserAvatar(null); // or set a default value if needed
        }
        return comment;
    }

    PostCommentReply enrichReplyWithUserProfile(PostCommentReply reply) {
        UserProfileResponse userProfile = userProfileClient.getUserProfile(reply.getUserId());
        reply.setUsername(userProfile.getUsername());
        if (userProfile.getImage() != null) {
            reply.setUserAvatar(userProfile.getImage().getPath());
        } else {
            reply.setUserAvatar(null); // or set a default value if needed
        }
        return reply;
    }
}