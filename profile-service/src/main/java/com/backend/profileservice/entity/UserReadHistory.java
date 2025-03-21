package com.backend.profileservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

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
