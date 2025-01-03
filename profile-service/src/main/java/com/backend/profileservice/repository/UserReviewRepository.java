package com.backend.profileservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.backend.profileservice.entity.UserReview;

@Repository
public interface UserReviewRepository extends MongoRepository<UserReview, String> {
    UserReview findByUserIdAndNovelId(String userId, String novelId);

    void deleteByUserIdAndNovelId(String userId, String novelId);

    Page<UserReview> findAllByNovelId(String novelId, Pageable pageable);
}
