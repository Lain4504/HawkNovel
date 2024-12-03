package com.backend.novelservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class NovelChapterCommentReply {
    String id;
    String commentId;
    String userId;
    String replyContent;
    String replyTo;
    String createdDate;
    String updateDateTime;
}
