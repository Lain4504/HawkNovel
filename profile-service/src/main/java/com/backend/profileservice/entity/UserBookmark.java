package com.backend.profileservice.entity;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "user_bookmark")
public class UserBookmark {
    @MongoId
    String id;

    String userId;
    String novelId;
    String novelTitle;
    String novelChapterId;
    String novelChapterTitle;
    String contentNote;
    Instant createdAt;
    Instant updatedAt;
}
