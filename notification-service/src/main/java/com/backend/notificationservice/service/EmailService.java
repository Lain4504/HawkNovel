package com.backend.notificationservice.service;

import com.backend.event.NotificationEvent;
import com.backend.notificationservice.enums.NotificationTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Async
    public void sendActivationEmail(NotificationEvent event) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Lấy thông tin từ NotificationEvent
        String recipient = event.getRecipient();
        String subject = (String) event.getParam().get("subject");
        String activationToken = (String) event.getParam().get("activeToken");
        // Tạo liên kết kích hoạt
        String activationLink = "http://localhost:5173/activation/" + activationToken;
        // Nội dung HTML
        String html = NotificationTemplate.ACTIVATION_EMAIL.getTemplate()
                .formatted(activationLink);
        // Cấu hình email
        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(html, true); // true indicates the content is HTML
        helper.setFrom("no-reply@foreverbookstore.com");
        // Gửi email
        javaMailSender.send(message);
    }

    @Async
    public void sendResetPasswordEmail(NotificationEvent event) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        var token = event.getParam().get("resetToken");
        String recipient = event.getRecipient();
        String subject = (String) event.getParam().get("subject");
        // Reset link
        String resetLink = "http://localhost:5173/reset-password/" + token;

        // HTML Content
        String emailContent = NotificationTemplate.RESET_PASSWORD_EMAIL.getTemplate()
                .formatted(resetLink);

        // Configure Email
        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(emailContent, true);
        helper.setFrom("no-reply@foreverbookstore.com");
        // Send Email
        javaMailSender.send(message);
    }
}
