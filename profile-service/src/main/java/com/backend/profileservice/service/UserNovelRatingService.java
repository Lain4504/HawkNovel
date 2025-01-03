package com.backend.profileservice.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.backend.profileservice.dto.request.UserNovelRatingRequest;
import com.backend.profileservice.dto.response.UserNovelRatingResponse;
import com.backend.profileservice.entity.UserNovelRating;
import com.backend.profileservice.mapper.UserNovelRatingMapper;
import com.backend.profileservice.repository.UserNovelRatingRepository;
import com.backend.profileservice.repository.UserProfileRepository;
import com.backend.profileservice.repository.httpclient.NovelServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class UserNovelRatingService {
    UserNovelRatingRepository userNovelRatingRepository;
    UserNovelRatingMapper userNovelRatingMapper;
    UserProfileRepository userProfileRepository;
    NovelServiceClient client;

    public UserNovelRatingResponse rateNovel(UserNovelRatingRequest request) {
        if (!userProfileRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User does not exist");
        }
        UserNovelRating userNovelRating = userNovelRatingMapper.toUserNovelRating(request);
        client.updateNovelRating(request.getNovelId(), request.getRating());
        userNovelRating = userNovelRatingRepository.save(userNovelRating);
        return userNovelRatingMapper.toUserNovelRatingResponse(userNovelRating);
    }

    public UserNovelRatingResponse updateRating(UserNovelRatingRequest request) {
        if (!userProfileRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User does not exist");
        }
        UserNovelRating userNovelRating =
                userNovelRatingRepository.findByUserIdAndNovelId(request.getUserId(), request.getNovelId());
        long oldRating = userNovelRating.getRating();
        long newRating = request.getRating();
        userNovelRatingMapper.updateUserNovelRating(userNovelRating, request);
        Map<String, Long> ratings = Map.of("newRating", newRating, "oldRating", oldRating);
        client.updateNovelRatingWithSameUser(request.getNovelId(), ratings);
        userNovelRatingRepository.save(userNovelRating);
        return userNovelRatingMapper.toUserNovelRatingResponse(userNovelRating);
    }

    public boolean hasRatedNovel(String userId, String novelId) {
        return userNovelRatingRepository.findByUserIdAndNovelId(userId, novelId) != null;
    }

    public UserNovelRatingResponse getRating(String userId, String novelId) {
        UserNovelRating userNovelRating = userNovelRatingRepository.findByUserIdAndNovelId(userId, novelId);
        return userNovelRatingMapper.toUserNovelRatingResponse(userNovelRating);
    }
}
