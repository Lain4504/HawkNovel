package com.backend.novelservice.service;

import com.backend.dto.response.PageResponse;
import com.backend.novelservice.dto.request.NovelCategoryRequest;
import com.backend.novelservice.dto.response.NovelCategoryResponse;
import com.backend.novelservice.entity.NovelCategory;
import com.backend.novelservice.mapper.NovelCategoryMapper;
import com.backend.novelservice.repository.NovelCategoryRepository;
import com.backend.utils.DateTimeFormatterUtils;
import lombok.AccessLevel;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NovelCategoryService {
    NovelCategoryMapper novelCategoryMapper;
    NovelCategoryRepository novelCategoryRepository;
    DateTimeFormatterUtils dateTimeFormatter;

    public NovelCategoryResponse createNovelCategory(NovelCategoryRequest request) {
        NovelCategory newNovelCategory = NovelCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .build();
        newNovelCategory = novelCategoryRepository.save(newNovelCategory);
        return novelCategoryMapper.toNovelCategoryResponse(newNovelCategory);
    }

    public NovelCategoryResponse getNovelCategoryById(String id) {
        var category = novelCategoryRepository.findById(id).orElse(null);
        return novelCategoryMapper.toNovelCategoryResponse(category);
    }

    public PageResponse<NovelCategoryResponse> getNovelCategories(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = novelCategoryRepository.findAll(pageable);
        var categoryList = pageData.getContent().stream().map(novelCategory -> {
            var categoryResponse = novelCategoryMapper.toNovelCategoryResponse(novelCategory);
            categoryResponse.setCreated(dateTimeFormatter.format(novelCategory.getCreatedDate()));
            return categoryResponse;
        }).toList();
        return PageResponse.<NovelCategoryResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(categoryList)
                .build();
    }

    public void deleteNovelCategory(String id) {
        novelCategoryRepository.deleteById(id);
    }

    public NovelCategoryResponse updateNovelCategory(String id, NovelCategoryRequest request) {
        var category = novelCategoryRepository.findById(id).orElse(null);
        if (category == null) {
            return null;
        }
        novelCategoryMapper.updateNovelCategory(category, request);
        var savedCategory = novelCategoryRepository.save(category);
        return novelCategoryMapper.toNovelCategoryResponse(savedCategory);
    }

    public List<NovelCategoryResponse> getAllNovelCategories() {
        var categories = novelCategoryRepository.findAll();
        return categories.stream().map(novelCategoryMapper::toNovelCategoryResponse).toList();
    }
}
