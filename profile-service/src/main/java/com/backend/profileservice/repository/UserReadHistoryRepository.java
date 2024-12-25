package com.backend.profileservice.repository;

import com.backend.profileservice.entity.UserReadHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserReadHistoryRepository extends MongoRepository<UserReadHistory, String> {
    List<UserReadHistory> findByUserId(String userId);

    Optional<UserReadHistory> findByUserIdAndNovelId(String userId, String novelId);

    List<UserReadHistory> findByUserIdAndType(String userId, String type);
}
