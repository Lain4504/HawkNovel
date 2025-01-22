package com.backend.profileservice.service;

import java.time.Instant;
import java.util.List;

import com.backend.utils.DateTimeFormatterUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.backend.dto.response.PageResponse;
import com.backend.profileservice.dto.request.UserBookmarkRequest;
import com.backend.profileservice.dto.response.UserBookmarkResponse;
import com.backend.profileservice.entity.UserBookmark;
import com.backend.profileservice.mapper.UserBookmarkMapper;
import com.backend.profileservice.repository.UserBookmarkRepository;
import com.backend.profileservice.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserBookmarkService {
    UserBookmarkRepository userBookmarkRepository;
    UserProfileRepository userProfileRepository;
    UserBookmarkMapper userBookmarkMapper;
    DateTimeFormatterUtils dateTimeFormatter;

    public UserBookmarkResponse saveUserBookmark(String userId, UserBookmarkRequest request) {
        if (!userProfileRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("User does not exist");
        }
        UserBookmark userBookmark = userBookmarkMapper.toUserBookmark(request);
        userBookmark.setCreatedAt(Instant.now());
        userBookmark.setUpdatedAt(Instant.now());
        userBookmark = userBookmarkRepository.save(userBookmark);
        return userBookmarkMapper.toUserBookmarkResponse(userBookmark);
    }

    public void deleteUserBookmark(String id) {
        userBookmarkRepository.deleteById(id);
    }

    public PageResponse<UserBookmarkResponse> getUserBookmarks(String userId, int page, int size) {
        if (!userProfileRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("User does not exist");
        }
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = userBookmarkRepository.findAllByUserId(userId, pageable);
        var userBookmarks = pageData.getContent().stream()
                .map(userBookmark -> {
                    var response = userBookmarkMapper.toUserBookmarkResponse(userBookmark);
                    response.setCreated(dateTimeFormatter.format(userBookmark.getCreatedAt()));
                    return response;
                })
                .toList();
        return PageResponse.<UserBookmarkResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(userBookmarks)
                .build();
    }

    public List<UserBookmarkResponse> getBookmarkByChapter(String userId, String chapterId, String novelId) {
        if (!userProfileRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("User does not exist");
        }
        return userBookmarkRepository.findByUserIdAndNovelChapterIdAndNovelId(userId, chapterId, novelId).stream()
                .map(userBookmarkMapper::toUserBookmarkResponse)
                .toList();
    }
}
