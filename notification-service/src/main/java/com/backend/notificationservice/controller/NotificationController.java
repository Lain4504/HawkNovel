package com.backend.notificationservice.controller;

import com.backend.dto.response.ApiResponse;
import com.backend.dto.response.PageResponse;
import com.backend.event.NotificationEvent;
import com.backend.notificationservice.entity.Notification;
import com.backend.notificationservice.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    @Autowired
    NotificationService notificationService;

    @KafkaListener(topics = "comment-notification")
    public void listen(NotificationEvent message) {
        log.info("Received message: {}", message);
        try {
            notificationService.sendNotification(message);
            log.info("Notification sent to: {}", message.getRecipient());
        } catch (MessagingException e) {
            log.error("Error sending notification to {}: {}", message.getRecipient(), e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    ApiResponse<PageResponse<Notification>> getNotificationByUserId(
            @PathVariable("userId") String userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        return ApiResponse.<PageResponse<Notification>>builder()
                .result(notificationService.getNotifications(userId, page, size))
                .build();
    }
}
