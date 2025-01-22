package com.backend.commentservice.service;

import com.backend.commentservice.dto.request.NovelChapterCommentReplyRequest;
import com.backend.commentservice.dto.request.NovelChapterCommentRequest;
import com.backend.commentservice.dto.response.NovelChapterCommentReplyResponse;
import com.backend.commentservice.dto.response.NovelChapterCommentResponse;
import com.backend.commentservice.entity.NovelChapterComment;
import com.backend.commentservice.entity.NovelChapterCommentReply;
import com.backend.commentservice.mapper.NovelChapterCommentMapper;
import com.backend.commentservice.mapper.NovelChapterCommentReplyMapper;
import com.backend.commentservice.repository.NovelChapterCommentReplyRepository;
import com.backend.commentservice.repository.NovelChapterCommentRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NovelChapterCommentService {
    NovelChapterCommentRepository novelChapterCommentRepository;
    NovelChapterCommentReplyRepository novelChapterCommentReplyRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    UserProfileClient userProfileClient;
    DateTimeFormatterUtils dateTimeFormatter;
    NovelChapterCommentMapper novelChapterCommentMapper;
    NovelChapterCommentReplyMapper novelChapterCommentReplyMapper;

    public PageResponse<NovelChapterCommentResponse> getAllComments(String chapterId, int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("createdDate"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = novelChapterCommentRepository.findAllByChapterId(chapterId, pageable);
        var commentList = pageData.getContent().stream()
                .map(this::enrichCommentWithUserProfile)
                .map(novelChapterComment -> {
                    NovelChapterCommentResponse response = novelChapterCommentMapper.toNovelChapterCommentResponse(novelChapterComment);
                    response.setCreated(dateTimeFormatter.format(novelChapterComment.getCreatedDate()));
                    return response;
                })
                .collect(Collectors.toList());
        return PageResponse.<NovelChapterCommentResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(commentList)
                .build();

    }

    public NovelChapterCommentResponse createComment(NovelChapterCommentRequest request) {
        NovelChapterComment novelChapterComment = novelChapterCommentMapper.toNovelChapterComment(request);
        novelChapterComment.setCreatedDate(Instant.now());
        novelChapterComment.setUpdateDateTime(Instant.now());
        novelChapterComment.setUsername(userProfileClient.getUserProfile(novelChapterComment.getUserId()).getUsername());
        if (!novelChapterComment.getOwnerId().equals(novelChapterComment.getUserId())) {
            NotificationEvent event = NotificationEvent
                    .builder()
                    .channel("NOVEL_CHAPTER")
                    .recipient(novelChapterComment.getOwnerId())
                    .templateCode("NOVEL_CHAPTER_COMMENT_OWNER_NOTIFICATION")
                    .param(Map.of("fromUser", novelChapterComment.getUsername(), "inLocation", novelChapterComment.getChapterName(),
                            "content", novelChapterComment.getContent()))
                    .build();
            //Publish message to kafka
            kafkaTemplate.send("comment-notification", event);
        }
        return novelChapterCommentMapper.toNovelChapterCommentResponse(novelChapterCommentRepository.save(novelChapterComment));
    }

    public NovelChapterCommentResponse updateComment(String id, NovelChapterCommentRequest request) {
        return novelChapterCommentRepository.findById(id)
                .map(existingComment -> {
                    novelChapterCommentMapper.updateNovelChapterComment(existingComment, request);
                    existingComment.setUpdateDateTime(Instant.now());
                    return novelChapterCommentMapper.toNovelChapterCommentResponse(novelChapterCommentRepository.save(existingComment));
                })
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    public void deleteComment(String id) {
        novelChapterCommentRepository.deleteById(id);
    }

    public PageResponse<NovelChapterCommentReplyResponse> getAllRepliesByCommentId(String commentId, int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("createdDate"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = novelChapterCommentReplyRepository.findAllByCommentId(commentId, pageable);
        var replyList = pageData.getContent().stream()
                .map(this::enrichReplyWithUserProfile)
                .map(novelChapterCommentReply -> {
                    NovelChapterCommentReplyResponse response = novelChapterCommentReplyMapper.toNovelChapterCommentReplyResponse(novelChapterCommentReply);
                    response.setCreated(dateTimeFormatter.format(novelChapterCommentReply.getCreatedDate()));
                    return response;
                })
                .collect(Collectors.toList());
        return PageResponse.<NovelChapterCommentReplyResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(replyList)
                .build();

    }

    public NovelChapterCommentReplyResponse createReply(NovelChapterCommentReplyRequest request) {
        NovelChapterCommentReply novelChapterCommentReply = novelChapterCommentReplyMapper.toNovelChapterCommentReply(request);
        novelChapterCommentReply.setCreatedDate(Instant.now());
        novelChapterCommentReply.setUpdateDateTime(Instant.now());
        novelChapterCommentReply.setUsername(userProfileClient.getUserProfile(novelChapterCommentReply.getUserId()).getUsername());
        novelChapterCommentRepository.findById(novelChapterCommentReply.getCommentId())
                .ifPresent(comment -> {
                    comment.setReplyCount(comment.getReplyCount() + 1);
                    novelChapterCommentRepository.save(comment);
                });
        if (novelChapterCommentReply.getUserIdOfReplyTo() != null && !novelChapterCommentReply.getUserIdOfReplyTo().equals(novelChapterCommentReply.getUserId())) {
            NotificationEvent event = NotificationEvent
                    .builder()
                    .channel("NOVEL_CHAPTER")
                    .recipient(novelChapterCommentReply.getUserIdOfReplyTo())
                    .templateCode("NOVEL_CHAPTER_COMMENT_REPLY_NOTIFICATION")
                    .param(Map.of("fromUser", novelChapterCommentReply.getUsername(), "inLocation", novelChapterCommentReply.getChapterName(),
                            "content", novelChapterCommentReply.getReplyContent()))
                    .build();
            //Publish message to kafka
            kafkaTemplate.send("comment-notification", event);
        }
        return novelChapterCommentReplyMapper.toNovelChapterCommentReplyResponse(novelChapterCommentReplyRepository.save(novelChapterCommentReply));
    }

    public NovelChapterCommentReplyResponse updateReply(String id, NovelChapterCommentReplyRequest request) {
        return novelChapterCommentReplyRepository.findById(id)
                .map(existingReply -> {
                    novelChapterCommentReplyMapper.updateNovelChapterCommentReply(existingReply, request);
                    existingReply.setUpdateDateTime(Instant.now());
                    return novelChapterCommentReplyMapper.toNovelChapterCommentReplyResponse(novelChapterCommentReplyRepository.save(existingReply));
                })
                .orElseThrow(() -> new RuntimeException("Reply not found"));
    }

    public void deleteReply(String id) {
        novelChapterCommentReplyRepository.findById(id)
                .ifPresent(reply -> {
                    novelChapterCommentRepository.findById(reply.getCommentId())
                            .ifPresent(comment -> {
                                comment.setReplyCount(comment.getReplyCount() - 1);
                                novelChapterCommentRepository.save(comment);
                            });
                });
        novelChapterCommentReplyRepository.deleteById(id);
    }

    NovelChapterComment enrichCommentWithUserProfile(NovelChapterComment comment) {
        UserProfileResponse userProfile = userProfileClient.getUserProfile(comment.getUserId());
        comment.setUsername(userProfile.getUsername());
        if (userProfile.getImage() != null) {
            comment.setUserAvatar(userProfile.getImage().getPath());
        } else {
            comment.setUserAvatar(null); // or set a default value if needed
        }
        return comment;
    }

    NovelChapterCommentReply enrichReplyWithUserProfile(NovelChapterCommentReply reply) {
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
