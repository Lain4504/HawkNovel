package com.backend.identityservice.repository;

import com.backend.identityservice.entity.User;
import com.backend.identityservice.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenRepository extends JpaRepository<UserToken, String> {

}
