package com.backend.profileservice.entity;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "user_read_history")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserReadHistory {
    @MongoId
    String id;

    String userId;
    String novelId;
    String novelTitle;
    String novelChapterId;
    String novelChapterTitle;
    LocalDateTime createdDate;
    LocalDateTime modifiedDate;
}
