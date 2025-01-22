package com.backend.commentservice.service;

import com.backend.commentservice.dto.request.NovelCommentReplyRequest;
import com.backend.commentservice.dto.request.NovelCommentRequest;
import com.backend.commentservice.dto.response.NovelCommentReplyResponse;
import com.backend.commentservice.dto.response.NovelCommentResponse;
import com.backend.commentservice.entity.NovelComment;
import com.backend.commentservice.entity.NovelCommentReply;
import com.backend.commentservice.mapper.NovelCommentMapper;
import com.backend.commentservice.mapper.NovelCommentReplyMapper;
import com.backend.commentservice.repository.NovelCommentReplyRepository;
import com.backend.commentservice.repository.NovelCommentRepository;
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
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NovelCommentService {
    NovelCommentRepository novelCommentRepository;
    NovelCommentReplyRepository novelCommentReplyRepository;
    NovelCommentMapper novelCommentMapper;
    NovelCommentReplyMapper novelCommentReplyMapper;
    UserProfileClient userProfileClient;
    DateTimeFormatterUtils dateTimeFormatter;
    KafkaTemplate<String, Object> kafkaTemplate;

    public PageResponse<NovelCommentResponse> getAllComments(String novelId, int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("createdDate"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = novelCommentRepository.findAllByNovelId(novelId, pageable);
        var commentList = pageData.getContent().stream()
                .map((this::enrichCommentWithUserProfile))
                .map(novelComment -> {
                    NovelCommentResponse response = novelCommentMapper.toNovelCommentResponse(novelComment);
                    response.setCreated(dateTimeFormatter.format(novelComment.getCreatedDate()));
                    return response;
                })
                .collect(Collectors.toList());
        return PageResponse.<NovelCommentResponse>builder()
                .currentPage(page)
                .totalPages(pageData.getTotalPages())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(commentList)
                .build();

    }

    public NovelCommentResponse createComment(NovelCommentRequest request) {
        NovelComment novelComment = novelCommentMapper.toNovelComment(request);
        novelComment.setCreatedDate(Instant.now());
        novelComment.setUpdateDateTime(Instant.now());
        novelComment.setUsername(userProfileClient.getUserProfile(novelComment.getUserId()).getUsername());
        if (!novelComment.getOwnerId().equals(novelComment.getUserId())) {
            NotificationEvent event = NotificationEvent
                    .builder()
                    .channel("NOVEL")
                    .recipient(novelComment.getOwnerId())
                    .templateCode("NOVEL_COMMENT_OWNER_NOTIFICATION")
                    .param(Map.of("fromUser", novelComment.getUsername(), "inLocation", novelComment.getNovelName(),
                            "content", novelComment.getContent()))
                    .build();
            //Publish message to kafka
            kafkaTemplate.send("comment-notification", event);
        }
        return novelCommentMapper.toNovelCommentResponse(novelCommentRepository.save(novelComment));
    }

    public NovelCommentResponse updateComment(String id, NovelCommentRequest request) {
        return novelCommentRepository.findById(id)
                .map(existingComment -> {
                    novelCommentMapper.updateNovelCommentFromRequest(existingComment, request);
                    existingComment.setUpdateDateTime(Instant.now());
                    return novelCommentMapper.toNovelCommentResponse(novelCommentRepository.save(existingComment));
                })
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    public void deleteComment(String id) {
        novelCommentRepository.deleteById(id);
    }

    public PageResponse<NovelCommentReplyResponse> getAllRepliesByCommentId(String commentId, int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = novelCommentReplyRepository.findAllByCommentId(commentId, pageable);
        var replyList = pageData.getContent().stream()
                .map(this::enrichReplyWithUserProfile)
                .map(reply -> {
                    NovelCommentReplyResponse response = novelCommentReplyMapper.toNovelCommentReplyResponse(reply);
                    response.setCreated(dateTimeFormatter.format(reply.getCreatedDate()));
                    return response;
                })
                .collect(Collectors.toList());
        return PageResponse.<NovelCommentReplyResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(replyList)
                .build();
    }

    public NovelCommentReplyResponse createReply(NovelCommentReplyRequest request) {
        NovelCommentReply novelCommentReply = novelCommentReplyMapper.toNovelCommentReply(request);
        novelCommentReply.setCreatedDate(Instant.now());
        novelCommentReply.setUpdateDateTime(Instant.now());
        novelCommentReply.setUsername(userProfileClient.getUserProfile(novelCommentReply.getUserId()).getUsername());
        novelCommentRepository.findById(novelCommentReply.getCommentId())
                .ifPresent(comment -> {
                    comment.setReplyCount(comment.getReplyCount() + 1);
                    novelCommentRepository.save(comment);
                });
        if (!novelCommentReply.getUserIdOfReplyTo().equals(novelCommentReply.getUserId())) {
            NotificationEvent event = NotificationEvent.builder()
                    .channel("NOVEL")
                    .recipient(novelCommentReply.getUserIdOfReplyTo())
                    .templateCode("NOVEL_COMMENT_REPLY_NOTIFICATION")
                    .param(Map.of("fromUser", novelCommentReply.getUsername(), "inLocation", novelCommentReply.getNovelName(),
                            "content", novelCommentReply.getReplyContent())).build();
            //Publish message to kafka
            log.info("NotificationEvent: {}", event);
            kafkaTemplate.send("comment-notification", event);
        }
        return novelCommentReplyMapper.toNovelCommentReplyResponse(novelCommentReplyRepository.save(novelCommentReply));
    }

    public NovelCommentReplyResponse updateReply(String id, NovelCommentReplyRequest request) {
        return novelCommentReplyRepository.findById(id)
                .map(existingReply -> {
                    novelCommentReplyMapper.updateNovelCommentReplyFromRequest(existingReply, request);
                    existingReply.setUpdateDateTime(Instant.now());
                    return novelCommentReplyMapper.toNovelCommentReplyResponse(novelCommentReplyRepository.save(existingReply));
                })
                .orElseThrow(() -> new RuntimeException("Reply not found"));
    }

    public void deleteReply(String id) {
        novelCommentReplyRepository.findById(id)
                .ifPresent(reply -> {
                    novelCommentRepository.findById(reply.getCommentId())
                            .ifPresent(comment -> {
                                comment.setReplyCount(comment.getReplyCount() - 1);
                                novelCommentRepository.save(comment);
                            });
                });
        novelCommentReplyRepository.deleteById(id);
    }

    NovelComment enrichCommentWithUserProfile(NovelComment comment) {
        UserProfileResponse userProfile = userProfileClient.getUserProfile(comment.getUserId());
        comment.setUsername(userProfile.getUsername());
        if (userProfile.getImage() != null) {
            comment.setUserAvatar(userProfile.getImage().getPath());
        } else {
            comment.setUserAvatar(null); // or set a default value if needed
        }
        return comment;
    }

    NovelCommentReply enrichReplyWithUserProfile(NovelCommentReply reply) {
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
