package com.backend.profileservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserNovelRatingRequest {
    @NotNull(message = "userId cannot be null")
    String userId;
    @NotNull(message = "novelId cannot be null")
    String novelId;
    @Size(min = 1, max = 5, message = "rating must be between 1 and 5")
    @NotNull(message = "rating cannot be null")
    Integer rating;
}
