package com.backend.searchservice.controller;

import com.backend.dto.response.ApiResponse;
import com.backend.searchservice.dto.request.NovelSearchRequest;
import com.backend.searchservice.dto.response.NovelSearchResponse;
import com.backend.searchservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/novel")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {
    SearchService searchService;

    @GetMapping()
    ApiResponse<List<NovelSearchResponse>> getNovelsByElasticsearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Integer bookStatus,
            @RequestParam(required = false) Integer wordCountMin,
            @RequestParam(required = false) Integer wordCountMax,
            @RequestParam(required = false) String sort) throws IOException {
        NovelSearchRequest request = NovelSearchRequest.builder()
                .keyword(keyword)
                .categoryName(categoryName)
                .bookStatus(bookStatus)
                .wordCountMin(wordCountMin)
                .wordCountMax(wordCountMax)
                .sort(sort)
                .build();
        List<NovelSearchResponse> results = searchService.searchNovels(request);
        return ApiResponse.<List<NovelSearchResponse>>builder()
                .result(results).build();
    }
}