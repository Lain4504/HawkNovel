package com.backend.notificationservice.service;

import com.backend.dto.response.PageResponse;
import com.backend.event.NotificationEvent;
import com.backend.notificationservice.entity.Notification;
import com.backend.notificationservice.enums.NotificationTemplate;
import com.backend.notificationservice.repository.NotificationRepository;
import com.backend.utils.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    NotificationRepository notificationRepository;
    DateTimeFormatter dateTimeFormatter;

    public void sendNotification(NotificationEvent message) {
        String template;
        switch (message.getTemplateCode()) {
            case "POST_COMMENT_OWNER_NOTIFICATION":
                template = NotificationTemplate.POST_COMMENT_OWNER_NOTIFICATION.getTemplate()
                        .formatted(message.getParam().get("fromUser"), message.getParam().get("inLocation"), message.getParam().get("content"));
                break;
            case "POST_COMMENT_REPLY_NOTIFICATION":
                template = NotificationTemplate.POST_COMMENT_REPLY_NOTIFICATION.getTemplate()
                        .formatted(message.getParam().get("fromUser"), message.getParam().get("inLocation"), message.getParam().get("content"));
                break;
            case "NOVEL_COMMENT_OWNER_NOTIFICATION":
                template = NotificationTemplate.NOVEL_COMMENT_OWNER_NOTIFICATION.getTemplate()
                        .formatted(message.getParam().get("fromUser"), message.getParam().get("inLocation"), message.getParam().get("content"));
                break;
            case "NOVEL_COMMENT_REPLY_NOTIFICATION":
                template = NotificationTemplate.NOVEL_COMMENT_REPLY_NOTIFICATION.getTemplate()
                        .formatted(message.getParam().get("fromUser"), message.getParam().get("inLocation"), message.getParam().get("content"));
                break;
            case "NOVEL_CHAPTER_COMMENT_OWNER_NOTIFICATION":
                template = NotificationTemplate.NOVEL_CHAPTER_COMMENT_OWNER_NOTIFICATION.getTemplate()
                        .formatted(message.getParam().get("fromUser"), message.getParam().get("inLocation"), message.getParam().get("content"));
                break;
            case "NOVEL_CHAPTER_COMMENT_REPLY_NOTIFICATION":
                template = NotificationTemplate.NOVEL_CHAPTER_COMMENT_REPLY_NOTIFICATION.getTemplate()
                        .formatted(message.getParam().get("fromUser"), message.getParam().get("inLocation"), message.getParam().get("content"));
                break;
            default:
                throw new IllegalArgumentException("Unknown template code: " + message.getTemplateCode());
        }

        Notification notification = Notification.builder()
                .title("New Comment")
                .content(template)
                .isRead(false)
                .notificationType(message.getChannel())
                .userId(message.getRecipient())
                .createdDate(Instant.now())
                .build();
        notificationRepository.save(notification);
    }

    public PageResponse<Notification> getNotifications(String userId, int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("createdDate"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = notificationRepository.findAllByUserId(userId, pageable);
        var notificationList = pageData.getContent().stream().map(notification -> {
            notification.setCreated(dateTimeFormatter.format(notification.getCreatedDate()));
            return notification;
        }).toList();
        return PageResponse.<Notification>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(notificationList)
                .build();
    }
}