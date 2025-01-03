package com.backend.profileservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.backend.profileservice.entity.UserBookmark;

@Repository
public interface UserBookmarkRepository extends MongoRepository<UserBookmark, String> {

    Page<UserBookmark> findAllByUserId(String userId, Pageable pageable);

    List<UserBookmark> findByUserIdAndNovelChapterIdAndNovelId(String userId, String chapterId, String novelId);
}
