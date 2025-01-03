package com.backend.profileservice.entity;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "images")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Image {
    @MongoId
    String id;

    String name;
    String type;
    String path;
    Instant createdAt;
}
