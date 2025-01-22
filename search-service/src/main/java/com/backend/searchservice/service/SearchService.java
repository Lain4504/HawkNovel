package com.backend.searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.backend.event.NovelDataSenderEvent;
import com.backend.searchservice.dto.request.NovelSearchRequest;
import com.backend.searchservice.dto.response.NovelSearchResponse;
import com.backend.searchservice.entity.Novel;
import com.backend.searchservice.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SearchService {
    SearchRepository searchRepository;
    ElasticsearchClient elasticsearchClient;

    public void handleCreate(NovelDataSenderEvent data) {
        Map<String, Object> novelData = (Map<String, Object>) data.getParam().get("data");
        log.info("Novel data: " + novelData);
        Novel novel = mapToNovel(novelData);
        searchRepository.save(novel);
        log.info("Handled novel creation: " + novel);
    }

    public void handleUpdate(NovelDataSenderEvent data) {
        Map<String, Object> novelData = (Map<String, Object>) data.getParam().get("data");
        Novel novel = mapToNovel(novelData);
        searchRepository.save(novel);
        log.info("Handled novel update: " + novel);
    }

    public void handleDelete(NovelDataSenderEvent data) {
        Map<String, Object> novelData = (Map<String, Object>) data.getParam().get("data");
        Novel novel = mapToNovel(novelData);
        searchRepository.deleteById(novel.getId());
        log.info("Handled novel deletion: " + novel.getId());
    }

    private Novel mapToNovel(Map<String, Object> novelData) {
        return Novel.builder()
                .id(novelData.get("id") != null ? novelData.get("id").toString() : "")
                .categoryId(novelData.get("categoryId") != null ? new HashSet<>((List<String>) novelData.get("categoryId")) : new HashSet<>())
                .categoryName(novelData.get("categoryName") != null ? new HashSet<>((List<String>) novelData.get("categoryName")) : new HashSet<>())
                .bookName(novelData.get("bookName") != null ? novelData.get("bookName").toString() : "")
                .authorId(novelData.get("authorId") != null ? novelData.get("authorId").toString() : "")
                .authorName(novelData.get("authorName") != null ? novelData.get("authorName").toString() : "")
                .score(novelData.get("score") != null ? Double.parseDouble(novelData.get("score").toString()) : 0.0)
                .status(novelData.get("status") != null ? novelData.get("status").toString() : "")
                .visitCount(novelData.get("visitCount") != null ? Long.parseLong(novelData.get("visitCount").toString()) : 0L)
                .wordCount(novelData.get("wordCount") != null ? Long.parseLong(novelData.get("wordCount").toString()) : 0L)
                .lastChapterId(novelData.get("lastChapterId") != null ? novelData.get("lastChapterId").toString() : "")
                .lastChapterName(novelData.get("lastChapterName") != null ? novelData.get("lastChapterName").toString() : "")
                .lastChapterUpdateTime(novelData.get("lastChapterUpdateTime") != null ? Long.parseLong(novelData.get("lastChapterUpdateTime").toString()) : 0L)
                .image(novelData.get("image") != null ? novelData.get("image").toString() : "")
                .description(novelData.get("description") != null ? novelData.get("description").toString() : "")
                .build();
    }

    public List<NovelSearchResponse> searchNovels(NovelSearchRequest request) throws IOException {
        BoolQuery.Builder boolQuery = QueryBuilders.bool();
        if (request.getKeyword() != null) {
            boolQuery.should(QueryBuilders.match(m -> m.field("bookName").query(request.getKeyword()).boost(2.0f).fuzziness("AUTO")));
            boolQuery.should(QueryBuilders.match(m -> m.field("authorName").query(request.getKeyword()).fuzziness("AUTO")));
        }
        if (request.getCategoryName() != null) {
            boolQuery.filter(QueryBuilders.term(t -> t.field("categoryName").value(request.getCategoryName())));
        }
        if (request.getBookStatus() != null) {
            boolQuery.filter(QueryBuilders.term(t -> t.field("status").value(request.getBookStatus())));
        }
        if (request.getWordCountMin() != null) {
            boolQuery.filter(QueryBuilders.range(r -> r.field("wordCount").gte(JsonData.of(request.getWordCountMin()))));
        }
        if (request.getWordCountMax() != null) {
            boolQuery.filter(QueryBuilders.range(r -> r.field("wordCount").lte(JsonData.of(request.getWordCountMax()))));
        }

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("novel")
                .query(boolQuery.build()._toQuery())
        );
        log.info("Search request: " + searchRequest);
        SearchResponse<Novel> searchResponse = elasticsearchClient.search(searchRequest, Novel.class);
        log.info("Search response: " + searchResponse);
        return searchResponse.hits().hits().stream()
                .map(Hit::source).filter(Objects::nonNull)
                .map(novel -> NovelSearchResponse.builder()
                        .id(novel.getId())
                        .image(novel.getImage())
                        .bookName(novel.getBookName())
                        .categoryId(novel.getCategoryId())
                        .categoryName(novel.getCategoryName())
                        .status(novel.getStatus())
                        .authorId(novel.getAuthorId())
                        .authorName(novel.getAuthorName())
                        .description(novel.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
}