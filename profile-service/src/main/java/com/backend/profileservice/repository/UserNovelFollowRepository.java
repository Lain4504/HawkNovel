package com.backend.profileservice.repository;

import com.backend.profileservice.entity.UserNovelFollow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNovelFollowRepository extends MongoRepository<UserNovelFollow, String> {
    UserNovelFollow findByUserIdAndNovelId(String userId, String novelId);

    void deleteByUserIdAndNovelId(String userId, String novelId);

    UserNovelFollow findByUserId(String userId);

    List<UserNovelFollow> findAllByUserId(String userId);
}
