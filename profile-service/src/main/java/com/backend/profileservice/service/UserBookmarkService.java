package com.backend.profileservice.service;

import com.backend.dto.response.PageResponse;
import com.backend.profileservice.dto.request.UserBookmarkRequest;
import com.backend.profileservice.dto.response.UserBookmarkResponse;
import com.backend.profileservice.entity.UserBookmark;
import com.backend.profileservice.mapper.UserBookmarkMapper;
import com.backend.profileservice.repository.UserBookmarkRepository;
import com.backend.profileservice.repository.UserProfileRepository;
import com.backend.utils.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserBookmarkService {
    UserBookmarkRepository userBookmarkRepository;
    UserProfileRepository userProfileRepository;
    UserBookmarkMapper userBookmarkMapper;
    DateTimeFormatter dateTimeFormatter;

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
                }).toList();
        return PageResponse.<UserBookmarkResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(userBookmarks)
                .build();
    }
}
