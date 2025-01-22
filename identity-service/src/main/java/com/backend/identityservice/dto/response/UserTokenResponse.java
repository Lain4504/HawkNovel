package com.backend.identityservice.dto.response;

import java.time.LocalDateTime;

public class UserTokenResponse {
    String id;
    String userId;
    String accessToken;
    String refreshToken;
    LocalDateTime expiryTime;
}
