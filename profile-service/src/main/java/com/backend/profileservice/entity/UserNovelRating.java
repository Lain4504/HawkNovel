package com.backend.profileservice.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Document(collection = "user_novel_rating")
public class UserNovelRating {
    @MongoId
    String id;

    String userId;
    String novelId;
    Integer rating;
}
