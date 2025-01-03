package com.backend.profileservice.dto.response;

import lombok.Data;

@Data
public class ImgurResponse {
    private Data data;

    @lombok.Data
    public static class Data {
        private String id;
        private String link;
        private String type;
    }
}
