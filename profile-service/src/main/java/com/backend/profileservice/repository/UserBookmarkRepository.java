package com.backend.profileservice.repository;

import com.backend.profileservice.entity.UserBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBookmarkRepository extends MongoRepository<UserBookmark, String> {

    Page<UserBookmark> findAllByUserId(String userId, Pageable pageable);
}
