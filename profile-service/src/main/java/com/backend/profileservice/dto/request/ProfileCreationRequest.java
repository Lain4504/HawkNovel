package com.backend.profileservice.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileCreationRequest {
    @NotNull(message = "userId is mandatory")
    String userId;

    String username;
    LocalDate dateOfBirth;
    String bio;
    String gender;
    String avatar;
}
