package com.backend.profileservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.backend.profileservice.entity.Image;

@Repository
public interface ImageRepository extends MongoRepository<Image, String> {}
