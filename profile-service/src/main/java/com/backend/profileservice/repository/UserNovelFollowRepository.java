package com.backend.profileservice.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.backend.profileservice.entity.UserNovelFollow;

@Repository
public interface UserNovelFollowRepository extends MongoRepository<UserNovelFollow, String> {
    UserNovelFollow findByUserIdAndNovelId(String userId, String novelId);

    void deleteByUserIdAndNovelId(String userId, String novelId);

    UserNovelFollow findByUserId(String userId);

    List<UserNovelFollow> findAllByUserId(String userId);
}
