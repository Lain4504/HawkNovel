package com.backend.identityservice.repository;

import com.backend.identityservice.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserTokenRepository extends JpaRepository<UserToken, String> {

    boolean existsByUserId(String userId);

    Optional<UserToken> findByUserIdAndAccessToken(String userId, String accessToken);

    boolean existsByAccessToken(String accessToken);
}
