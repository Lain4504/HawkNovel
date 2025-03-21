package com.backend.notificationservice.controller;

import com.backend.event.NotificationEvent;
import com.backend.notificationservice.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailController {
    @Autowired
    EmailService emailService;

    @KafkaListener(topics = "onboard-successfully")
    public void listen(NotificationEvent message) {
        log.info("Received message: {}", message);
        try {
            emailService.sendActivationEmail(message);
            log.info("Email sent to: {}", message.getRecipient());
        } catch (MessagingException e) {
            log.error("Error sending email to {}: {}", message.getRecipient(), e.getMessage());
        }
    }

    @KafkaListener(topics = "reset-password-request")
    public void listenResetPassword(NotificationEvent message) {
        log.info("Received message: {}", message);
        try {
            emailService.sendResetPasswordEmail(message);
            log.info("Email sent to: {}", message.getRecipient());
        } catch (MessagingException e) {
            log.error("Error sending email to {}: {}", message.getRecipient(), e.getMessage());
        }
    }

}
