package com.backend.profileservice.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.dto.response.PageResponse;
import com.backend.profileservice.dto.request.UserNovelFollowRequest;
import com.backend.profileservice.dto.response.NovelDetailsResponse;
import com.backend.profileservice.dto.response.UserNovelFollowResponse;
import com.backend.profileservice.entity.UserNovelFollow;
import com.backend.profileservice.mapper.UserNovelFollowMapper;
import com.backend.profileservice.repository.UserNovelFollowRepository;
import com.backend.profileservice.repository.UserProfileRepository;
import com.backend.profileservice.repository.httpclient.NovelServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserNovelFollowService {
    UserNovelFollowRepository userNovelFollowRepository;
    UserNovelFollowMapper userNovelFollowMapper;
    UserProfileRepository userProfileRepository;
    NovelServiceClient novelServiceClient;

    public UserNovelFollowResponse followNovel(UserNovelFollowRequest request) {
        if (!userProfileRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User does not exist");
        }
        UserNovelFollow userNovelFollow = userNovelFollowMapper.toUserNovelFollow(request);
        novelServiceClient.updateNovelFollow(request.getNovelId(), true);
        userNovelFollowRepository.save(userNovelFollow);
        return userNovelFollowMapper.toUserNovelFollowResponse(userNovelFollow);
    }

    public void unfollowNovel(UserNovelFollowRequest request) {
        if (!userProfileRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User does not exist");
        }
        novelServiceClient.updateNovelFollow(request.getNovelId(), false);
        userNovelFollowRepository.deleteByUserIdAndNovelId(request.getUserId(), request.getNovelId());
    }

    public boolean isFollowingNovel(UserNovelFollowRequest request) {
        return userNovelFollowRepository.findByUserIdAndNovelId(request.getUserId(), request.getNovelId()) != null;
    }

    List<String> getFollowingNovelIds(String userId) {
        List<UserNovelFollow> userNovelFollows = userNovelFollowRepository.findAllByUserId(userId);
        return userNovelFollows.stream()
                .map(UserNovelFollow::getNovelId) //  Chỉ trích xuất novelId
                .toList();
    }

    public PageResponse<NovelDetailsResponse> getFollowingNovelsWithDetails(String userId, int page, int size) {
        if (!userProfileRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("User does not exist");
        }
        List<String> novelIds = getFollowingNovelIds(userId);
        if (novelIds.isEmpty()) {
            return PageResponse.<NovelDetailsResponse>builder()
                    .currentPage(page)
                    .pageSize(size)
                    .totalPages(0)
                    .totalElements(0)
                    .data(Collections.emptyList())
                    .build();
        }
        List<NovelDetailsResponse> novelDetails = novelServiceClient.getNovelDetails(novelIds);
        int start = Math.min((page - 1) * size, novelDetails.size());
        int end = Math.min(start + size, novelDetails.size());
        List<NovelDetailsResponse> pagedNovelDetails = novelDetails.subList(start, end);
        return PageResponse.<NovelDetailsResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages((int) Math.ceil((double) novelDetails.size() / size))
                .totalElements(novelDetails.size())
                .data(pagedNovelDetails)
                .build();
    }
}
