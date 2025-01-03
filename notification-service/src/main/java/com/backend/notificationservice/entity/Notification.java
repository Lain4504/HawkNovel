package com.backend.notificationservice.entity;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(value = "notification")
public class Notification {
    @MongoId
    String id;

    String title;
    String content;
    Boolean isRead;
    String notificationType;
    String userId;
    Instant createdDate;
    String created;
}
