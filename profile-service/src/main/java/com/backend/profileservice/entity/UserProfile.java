package com.backend.profileservice.entity;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(value = "user_profile")
public class UserProfile {
    @MongoId
    String id;

    String userId;
    String username;
    LocalDate dateOfBirth;
    String bio;
    String gender;

    @DBRef
    Image image;

    Instant createdAt;
}
