package com.backend.profileservice.repository;

import com.backend.profileservice.entity.UserNovelRating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNovelRatingRepository extends MongoRepository<UserNovelRating, String> {
    UserNovelRating findByUserIdAndNovelId(String userId, String novelId);

    void deleteByUserIdAndNovelId(String userId, String novelId);
}
