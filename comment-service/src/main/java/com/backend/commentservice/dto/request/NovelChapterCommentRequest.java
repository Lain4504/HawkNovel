package com.backend.commentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
public class NovelChapterCommentRequest {
    @NotNull(message = "Chapter ID is mandatory")
    String chapterId;
    @NotBlank(message = "Chapter name is mandatory")
    String chapterName;
    @NotNull(message = "User ID is mandatory")
    String userId;
    @NotBlank(message = "Content is mandatory")
    @Size(max = 500, message = "Content must be between 1 and 500 characters")
    String content;
    @NotNull(message = "Owner ID is mandatory")
    String ownerId;
}
