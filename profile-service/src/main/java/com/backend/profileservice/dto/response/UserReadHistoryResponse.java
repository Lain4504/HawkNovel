package com.backend.profileservice.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserReadHistoryResponse {
    String id;
    String userId;
    String novelId;
    String novelTitle;
    String novelChapterId;
    String novelChapterTitle;
    LocalDateTime createdDate;
    LocalDateTime modifiedDate;
}
