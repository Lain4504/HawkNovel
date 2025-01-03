package com.backend.notificationservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.backend.notificationservice.entity.Notification;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findAllByUserId(String userId, Pageable pageable);
}
