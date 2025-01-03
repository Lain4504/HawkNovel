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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(value = "user_review")
public class UserReview {
    @MongoId
    String id;

    String userId;
    String userName;
    String novelId;
    String review;
    Instant createdAt;
    Instant updatedAt;
}
