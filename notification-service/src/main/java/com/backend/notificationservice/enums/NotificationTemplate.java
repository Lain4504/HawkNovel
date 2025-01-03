package com.backend.notificationservice.enums;

import lombok.Getter;

@Getter
public enum NotificationTemplate {
    ACTIVATION_EMAIL(
            """
			<html>
			<head></head>
			<body>
				<div>
					<h2>Welcome to our platform!</h2>
					<p>Thank you for registering with us. To activate your account, please click the link below:</p>
					<p><a href='%s'>Activate Account</a></p>
					<p>If you did not request this activation, please ignore this email.</p>
					<p>Best regards,</p>
					<p>The Forever Bookstore Team</p>
				</div>
			</body>
			</html>
			"""),

    RESET_PASSWORD_EMAIL(
            """
			<html>
			<head></head>
			<body>
				<div>
					<h2>Reset Your Password</h2>
					<p>We received a request to reset your password. If you requested this, click the link below:</p>
					<p><a href="%s">Reset Password</a></p>
					<p>If you did not request a password reset, please ignore this email.</p>
					<p>Best regards,</p>
					<p>The Forever Bookstore Team</p>
				</div>
			</body>
			</html>
			"""),
    POST_COMMENT_OWNER_NOTIFICATION("You have a new comment from %s on your post in %s: \"%s\""),
    POST_COMMENT_REPLY_NOTIFICATION("You have a new reply from %s on your comment in %s: \"%s\""),
    NOVEL_COMMENT_OWNER_NOTIFICATION("You have a new comment from %s on your novel in %s: \"%s\""),
    NOVEL_COMMENT_REPLY_NOTIFICATION("You have a new comment from %s on your novel chapter in %s: \"%s\""),
    NOVEL_CHAPTER_COMMENT_OWNER_NOTIFICATION("You have a new comment from %s on your novel chapter in %s: \"%s\""),
    NOVEL_CHAPTER_COMMENT_REPLY_NOTIFICATION("You have a new comment from %s on your post in %s: \"%s\""),
    ;

    private final String template;

    NotificationTemplate(String template) {
        this.template = template;
    }
}
