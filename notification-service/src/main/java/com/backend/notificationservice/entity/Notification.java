package com.backend.notificationservice.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

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
