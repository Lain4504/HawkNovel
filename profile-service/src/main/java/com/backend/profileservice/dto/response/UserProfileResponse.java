package com.backend.profileservice.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import com.backend.profileservice.entity.Image;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    String id;
    String userId;
    String username;
    LocalDate dateOfBirth;
    String bio;
    String gender;
    Image image;
    String created;
    Instant createdAt;
}
