package com.backend.profileservice.service;

import java.time.Instant;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.backend.dto.response.PageResponse;
import com.backend.profileservice.dto.request.UserReviewRequest;
import com.backend.profileservice.dto.response.NovelDetailsResponse;
import com.backend.profileservice.dto.response.UserReviewResponse;
import com.backend.profileservice.entity.UserReview;
import com.backend.profileservice.mapper.UserReviewMapper;
import com.backend.profileservice.repository.UserProfileRepository;
import com.backend.profileservice.repository.UserReviewRepository;
import com.backend.profileservice.repository.httpclient.NovelServiceClient;
import com.backend.utils.DateTimeFormatter;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserReviewService {
    UserReviewRepository userReviewRepository;
    UserReviewMapper userReviewMapper;
    DateTimeFormatter dateTimeFormatter;
    NovelServiceClient novelServiceClient;
    UserProfileRepository userProfileRepository;

    public UserReviewResponse createReview(UserReviewRequest request) {
        if (!userProfileRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User does not exist");
        }
        UserReview userReview = userReviewMapper.toUserReview(request);
        userReview.setCreatedAt(Instant.now());
        userReviewRepository.save(userReview);
        return userReviewMapper.toUserReviewResponse(userReview);
    }

    public UserReviewResponse updateReview(UserReviewRequest request) {
        if (!userProfileRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User does not exist");
        }
        userReviewMapper.updateUserReview(
                userReviewRepository.findByUserIdAndNovelId(request.getUserId(), request.getNovelId()), request);
        return userReviewMapper.toUserReviewResponse(userReviewRepository.save(userReviewMapper.toUserReview(request)));
    }

    public void deleteReview(UserReviewRequest request) {
        userReviewRepository.deleteByUserIdAndNovelId(request.getUserId(), request.getNovelId());
    }

    public PageResponse<UserReviewResponse> getReviewsByNovelId(String novelId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = userReviewRepository.findAllByNovelId(novelId, pageable);
        var novelIds = pageData.getContent().stream()
                .map(UserReview::getNovelId)
                .distinct()
                .toList();
        var novelDetails = novelServiceClient.getNovelDetails(novelIds);
        var novelDetailsMap =
                novelDetails.stream().collect(Collectors.toMap(NovelDetailsResponse::getNovelId, Function.identity()));
        var novelReviews = pageData.getContent().stream()
                .map(novelReview -> {
                    var userReviewResponse = userReviewMapper.toUserReviewResponse(novelReview);
                    userReviewResponse.setCreated(dateTimeFormatter.format(novelReview.getCreatedAt()));
                    var novelDetail = novelDetailsMap.get(novelReview.getNovelId());
                    var userProfile = userProfileRepository
                            .findByUserId(novelReview.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    userReviewResponse.setImage(userProfile.getImage().getPath());
                    userReviewResponse.setUserName(userProfile.getUsername());
                    if (novelDetail != null) {
                        userReviewResponse.setNovelName(novelDetail.getNovelName());
                    }
                    return userReviewResponse;
                })
                .toList();

        return PageResponse.<UserReviewResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(novelReviews)
                .build();
    }

    public PageResponse<UserReviewResponse> getLatestReview(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = userReviewRepository.findAll(pageable);
        var novelIds = userReviewRepository.findAll(pageable).stream()
                .map(UserReview::getNovelId)
                .distinct()
                .toList();
        var novelDetails = novelServiceClient.getNovelDetails(novelIds);
        var novelDetailsMap =
                novelDetails.stream().collect(Collectors.toMap(NovelDetailsResponse::getNovelId, Function.identity()));
        var novelReviews = pageData.getContent().stream()
                .map(novelReview -> {
                    var userReviewResponse = userReviewMapper.toUserReviewResponse(novelReview);
                    userReviewResponse.setCreated(dateTimeFormatter.format(novelReview.getCreatedAt()));
                    var novelDetail = novelDetailsMap.get(novelReview.getNovelId());
                    var userProfile = userProfileRepository
                            .findByUserId(novelReview.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    userReviewResponse.setImage(userProfile.getImage().getPath());
                    userReviewResponse.setUserName(userProfile.getUsername());
                    if (novelDetail != null) {
                        userReviewResponse.setNovelName(novelDetail.getNovelName());
                    }
                    return userReviewResponse;
                })
                .toList();
        return PageResponse.<UserReviewResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(novelReviews)
                .build();
    }
}
