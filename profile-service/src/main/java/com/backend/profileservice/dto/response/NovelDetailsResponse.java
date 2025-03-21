package com.backend.profileservice.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NovelDetailsResponse {
    String novelId;
    String novelName;
    String author;
    Integer chapterCount;
    String image;
    String novelChapterId;
    String novelChapterTitle;
}


