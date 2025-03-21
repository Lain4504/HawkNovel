package com.backend.profileservice.service;

import com.backend.dto.response.PageResponse;
import com.backend.profileservice.dto.request.UserReadHistoryRequest;
import com.backend.profileservice.dto.response.NovelDetailsResponse;
import com.backend.profileservice.dto.response.UserReadHistoryResponse;
import com.backend.profileservice.entity.UserReadHistory;
import com.backend.profileservice.mapper.UserReadHistoryMapper;
import com.backend.profileservice.repository.UserProfileRepository;
import com.backend.profileservice.repository.UserReadHistoryRepository;
import com.backend.profileservice.repository.httpclient.NovelServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserReadHistoryService {
    UserReadHistoryRepository userReadHistoryRepository;
    UserReadHistoryMapper userReadHistoryMapper;
    NovelServiceClient client;
    UserProfileRepository userProfileRepository;

    public List<UserReadHistoryResponse> getReadHistoryByUserId(String userId) {
        List<UserReadHistory> userReadHistories = userReadHistoryRepository.findByUserId(userId);
        return userReadHistories.stream()
                .map(userReadHistoryMapper::toUserReadHistoryResponse)
                .toList();
    }

    public UserReadHistoryResponse createOrUpdateHistory(UserReadHistoryRequest userReadHistory) {
        Optional<UserReadHistory> existingHistory = userReadHistoryRepository.findByUserIdAndNovelId(userReadHistory.getUserId(), userReadHistory.getNovelId());
        if (existingHistory.isPresent()) {
            UserReadHistory historyToUpdate = existingHistory.get();
            userReadHistoryMapper.updateUserReadHistory(historyToUpdate, userReadHistory);
            return userReadHistoryMapper.toUserReadHistoryResponse(userReadHistoryRepository.save(historyToUpdate));
        } else {
            UserReadHistory newHistory = userReadHistoryMapper.toUserReadHistory(userReadHistory);
            return userReadHistoryMapper.toUserReadHistoryResponse(userReadHistoryRepository.save(newHistory));
        }
    }

    public void delete(String id) {
        userReadHistoryRepository.deleteById(id);
    }

    public Optional<UserReadHistoryResponse> getById(String id) {
        return userReadHistoryRepository.findById(id)
                .map(userReadHistoryMapper::toUserReadHistoryResponse);
    }

    public PageResponse<NovelDetailsResponse> getReadHistoryWithDetails(String userId, int page, int size) {
        if (!userProfileRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("User does not exist");
        }
        List<UserReadHistory> readHistoryResponse = userReadHistoryRepository.findByUserId(userId);
        List<String> novelIds = getReadHistoryByUserId(userId).stream()
                .map(UserReadHistoryResponse::getNovelId)
                .toList();
        if (novelIds.isEmpty()) {
            return PageResponse.<NovelDetailsResponse>builder()
                    .currentPage(page)
                    .pageSize(size)
                    .totalPages(0)
                    .totalElements(0)
                    .data(Collections.emptyList())
                    .build();
        }
        List<NovelDetailsResponse> novelDetails = client.getNovelDetails(novelIds);
        Map<String, UserReadHistory> readHistoryMap = readHistoryResponse.stream()
                .collect(Collectors.toMap(UserReadHistory::getNovelId, history -> history));
        List<NovelDetailsResponse> enrichedNovelDetails = novelDetails.stream()
                .map(detail -> {
                    UserReadHistory history = readHistoryMap.get(detail.getNovelId());
                    detail.setNovelName(history.getNovelTitle());
                    detail.setNovelChapterId(history.getNovelChapterId());
                    detail.setNovelChapterTitle(history.getNovelChapterTitle());
                    return detail;
                }).toList();
        int start = Math.min((page - 1) * size, enrichedNovelDetails.size());
        int end = Math.min(start + size, enrichedNovelDetails.size());
        List<NovelDetailsResponse> pagedNovelDetails = enrichedNovelDetails.subList(start, end);
        return PageResponse.<NovelDetailsResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages((int) Math.ceil((double) enrichedNovelDetails.size() / size))
                .totalElements(enrichedNovelDetails.size())
                .data(pagedNovelDetails)
                .build();
    }
}