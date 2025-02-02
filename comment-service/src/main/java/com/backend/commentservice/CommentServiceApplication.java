package com.backend.commentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.backend")
@EnableFeignClients
public class CommentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommentServiceApplication.class, args);
    }
}
