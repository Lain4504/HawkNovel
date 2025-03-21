package com.backend.profileservice.repository.httpclient;

import com.backend.profileservice.configuration.AuthenticationRequestInterceptor;
import com.backend.profileservice.dto.response.NovelDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "novel-service", url = "${app.services.novel}", configuration = {AuthenticationRequestInterceptor.class})
public interface NovelServiceClient {
    @PostMapping(value = "/internal/novels", produces = MediaType.APPLICATION_JSON_VALUE)
    List<NovelDetailsResponse> getNovelDetails(@RequestBody List<String> novelIds);

    @PutMapping(value = "/internal/novels/rating-update/{novelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    void updateNovelRating(@PathVariable("novelId") String novelId, @RequestBody long rating);

    @PutMapping(value = "/internal/novels/rating-update-with-same-user/{novelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    void updateNovelRatingWithSameUser(@PathVariable("novelId") String novelId, @RequestBody Map<String, Long> ratings);

    @PutMapping(value = "/internal/novels/follow-update/{novelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    void updateNovelFollow(@PathVariable("novelId") String novelId, @RequestBody boolean isNewFollow);
}

