package com.backend.postservice.service;

import com.backend.dto.response.PageResponse;
import com.backend.postservice.dto.request.PostCategoryRequest;
import com.backend.postservice.dto.response.PostCategoryResponse;
import com.backend.postservice.entity.PostCategory;
import com.backend.postservice.mapper.PostCategoryMapper;
import com.backend.postservice.repository.PostCategoryRepository;
import com.backend.utils.DateTimeFormatterUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PostCategoryService {
    PostCategoryRepository postCategoryRepository;
    PostCategoryMapper postCategoryMapper;
    DateTimeFormatterUtils dateTimeFormatter;

    public PostCategoryResponse createPostCategory(PostCategoryRequest postCategory) {
        PostCategory newPostCategory = PostCategory.builder()
                .name(postCategory.getName())
                .description(postCategory.getDescription())
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .build();
        newPostCategory = postCategoryRepository.save(newPostCategory);
        return postCategoryMapper.toPostCategoryResponse(newPostCategory);
    }

    public PageResponse<PostCategoryResponse> getAllPostCategories(int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("createdDate"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = postCategoryRepository.findAll(pageable);
        var postCategoryList = pageData.getContent().stream().map(postCategory -> {
            var postCategoryResponse = postCategoryMapper.toPostCategoryResponse(postCategory);
            postCategoryResponse.setCreated(dateTimeFormatter.format(postCategory.getCreatedDate()));
            return postCategoryResponse;
        }).toList();
        return PageResponse.<PostCategoryResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(postCategoryList)
                .build();
    }

    public PostCategoryResponse getPostCategory(String postCategoryId) {
        var postCategory = postCategoryRepository.findById(postCategoryId)
                .orElseThrow(() -> new RuntimeException("Post category not found"));
        return postCategoryMapper.toPostCategoryResponse(postCategory);
    }

    public List<PostCategoryResponse> getAllPostCategories() {
        var postCategoryList = postCategoryRepository.findAll(Sort.by(Sort.Order.desc("createdDate")));
        return postCategoryList.stream().map(postCategoryMapper::toPostCategoryResponse).toList();
    }

    public void deletePostCategory(String postCategoryId) {
        var postCategory = postCategoryRepository.findById(postCategoryId)
                .orElseThrow(() -> new RuntimeException("Post category not found"));
        postCategoryRepository.delete(postCategory);
    }

    public PostCategoryResponse updatePostCategory(String postCategoryId, PostCategoryRequest postCategoryRequest) {
        var postCategory = postCategoryRepository.findById(postCategoryId)
                .orElseThrow(() -> new RuntimeException("Post category not found"));
        postCategory.setName(postCategoryRequest.getName());
        postCategory.setDescription(postCategoryRequest.getDescription());
        postCategory.setModifiedDate(Instant.now());
        postCategory = postCategoryRepository.save(postCategory);
        return postCategoryMapper.toPostCategoryResponse(postCategory);
    }
}
