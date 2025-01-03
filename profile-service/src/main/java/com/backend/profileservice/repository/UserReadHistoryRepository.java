package com.backend.profileservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.backend.profileservice.entity.UserReadHistory;

public interface UserReadHistoryRepository extends MongoRepository<UserReadHistory, String> {
    Optional<UserReadHistory> findByUserIdAndNovelId(String userId, String novelId);

    List<UserReadHistory> findByUserId(String userId);
}
