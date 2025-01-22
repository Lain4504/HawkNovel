package com.backend.novelservice.entity;

import com.backend.novelservice.enums.ChapterStatusEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(value = "chapter")
public class NovelChapter {
    @MongoId
    String id;
    String volumeId;
    Integer chapterNumber;
    String chapterTitle;
    String content;
    Long wordCount;
    Boolean isVip;
    Instant createdDate;
    Instant updateDate;
    @Enumerated(EnumType.STRING)
    ChapterStatusEnum status;
}
