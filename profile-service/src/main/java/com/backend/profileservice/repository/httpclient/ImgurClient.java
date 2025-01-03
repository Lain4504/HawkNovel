package com.backend.profileservice.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.backend.profileservice.dto.response.ImgurResponse;

@FeignClient(name = "imgurClient", url = "https://api.imgur.com/3")
public interface ImgurClient {
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ImgurResponse uploadImage(
            @RequestHeader("Authorization") String authorization, @RequestPart("image") MultipartFile image);

    @DeleteMapping(value = "/image/{deleteHash}")
    void deleteImage(
            @RequestHeader("Authorization") String authorization, @RequestParam("deleteHash") String deleteHash);
}
