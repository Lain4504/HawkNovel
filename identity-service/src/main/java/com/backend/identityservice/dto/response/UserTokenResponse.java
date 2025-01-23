package com.backend.identityservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTokenResponse {
    String id;
    String userId;
    String accessToken;
    String refreshToken;
    LocalDateTime expiryTime;
}
