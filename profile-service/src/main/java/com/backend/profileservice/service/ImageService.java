package com.backend.profileservice.service;

import com.backend.exception.AppException;
import com.backend.exception.ErrorCode;
import com.backend.profileservice.dto.response.ImgurResponse;
import com.backend.profileservice.entity.Image;
import com.backend.profileservice.repository.ImageRepository;
import com.backend.profileservice.repository.httpclient.ImgurClient;
import com.backend.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ImageService {
    ImageRepository imageRepository;
    FileUtils fileUtils;
    ImgurClient imgurClient;

    @Value("${imgur.client-id}")
    @NonFinal
    protected String clientId;

    public Image getImage(String id) {
        return imageRepository.findById(id).orElseThrow(() -> {
            throw new AppException(ErrorCode.IMAGE_NOT_FOUND);
        });
    }

    public Image uploadImage(MultipartFile file) {
        fileUtils.validateFile(file);

        try {
            String authorization = "Client-ID " + clientId;
            ImgurResponse response = imgurClient.uploadImage(authorization, file);
            ImgurResponse.Data data = response.getData();

            Image image = new Image();
            image.setName(data.getId());
            image.setType(data.getType());
            image.setPath(data.getLink());
            image.setCreatedAt(Instant.now());
            return imageRepository.save(image);
        } catch (Exception e) {
            throw new RuntimeException("Upload image error", e);
        }
    }

    public void deleteImage(String id) {
        Image image = imageRepository.findById(id).orElseThrow(() -> {
            throw new AppException(ErrorCode.IMAGE_NOT_FOUND);
        });

//        try {
//            String authorization = "Client-ID " + clientId;
//            imgurClient.deleteImage(authorization, image.getName());
//        } catch (Exception e) {
//            throw new RuntimeException("Delete image error", e);
//        }

        imageRepository.delete(image);
    }
}