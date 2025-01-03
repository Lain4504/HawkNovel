package com.backend.profileservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.backend.profileservice.entity.UserNovelRating;

@Repository
public interface UserNovelRatingRepository extends MongoRepository<UserNovelRating, String> {
    UserNovelRating findByUserIdAndNovelId(String userId, String novelId);

    void deleteByUserIdAndNovelId(String userId, String novelId);
}
