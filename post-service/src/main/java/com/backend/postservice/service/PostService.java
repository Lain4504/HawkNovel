package com.backend.postservice.service;

import com.backend.dto.response.PageResponse;
import com.backend.postservice.dto.request.PostRequest;
import com.backend.postservice.dto.response.PostResponse;
import com.backend.postservice.entity.Post;
import com.backend.postservice.entity.PostCategory;
import com.backend.postservice.mapper.PostMapper;
import com.backend.postservice.repository.PostCategoryRepository;
import com.backend.postservice.repository.PostRepository;
import com.backend.utils.DateTimeFormatterUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {
    PostRepository postRepository;
    PostCategoryRepository postCategoryRepository;
    PostMapper postMapper;
    DateTimeFormatterUtils dateTimeFormatter;

    public PostResponse createPost(PostRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .categoryId(request.getCategoryId())
                .userId(authentication.getName())
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .build();

        post = postRepository.save(post);
        PostCategory category = postCategoryRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return postMapper.toPostResponse(post, category);
    }

    public void deletePost(String postId) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        postRepository.delete(post);
    }

    public PostResponse updatePost(String postId, PostRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUserId().equals(authentication.getName())) {
            throw new RuntimeException("You are not allowed to update this post");
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategoryId(request.getCategoryId());
        post.setModifiedDate(Instant.now());
        post = postRepository.save(post);
        PostCategory category = postCategoryRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return postMapper.toPostResponse(post, category);
    }

    public PageResponse<PostResponse> getMyPosts(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Sort sort = Sort.by(Sort.Order.desc("createdDate"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = postRepository.findAllByUserId(userId, pageable);
        var postList = pageData.getContent().stream().map(post -> {
            PostCategory category = postCategoryRepository.findById(post.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            var postResponse = postMapper.toPostResponse(post, category);
            postResponse.setCreated(dateTimeFormatter.format(post.getCreatedDate()));
            return postResponse;
        }).toList();
        return PageResponse.<PostResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(postList)
                .build();
    }

    public PostResponse getPost(String postId) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        PostCategory category = postCategoryRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        var postResponse = postMapper.toPostResponse(post, category);
        postResponse.setCreated(dateTimeFormatter.format(post.getCreatedDate()));
        return postResponse;
    }

    public PageResponse<PostResponse> getAllPosts(int page, int size, String categoryId) {
        Sort sort = Sort.by(Sort.Order.desc("createdDate"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Post> pageData;

        if (categoryId != null && !categoryId.isEmpty()) {
            pageData = postRepository.findByCategoryId(categoryId, pageable);
        } else {
            pageData = postRepository.findAll(pageable);
        }

        var postList = pageData.getContent().stream().map(post -> {
            // Nếu categoryId là null, xử lý riêng
            PostCategory category = null;
            if (post.getCategoryId() != null) {
                category = postCategoryRepository.findById(post.getCategoryId())
                        .orElse(null);  // Tránh ném ngoại lệ
            }
            // Kiểm tra xem có category không, nếu không có thì để null hoặc gán giá trị mặc định
            var postResponse = postMapper.toPostResponse(post, category);
            postResponse.setCreated(dateTimeFormatter.format(post.getCreatedDate()));
            return postResponse;
        }).toList();

        return PageResponse.<PostResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(postList)
                .build();
    }
}