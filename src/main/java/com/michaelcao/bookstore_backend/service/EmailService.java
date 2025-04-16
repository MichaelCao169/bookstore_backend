// src/main/java/com/michaelcao/bookstore_backend/service/EmailService.java
package com.michaelcao.bookstore_backend.service;

import jakarta.mail.MessagingException; // Import exception
import jakarta.mail.internet.MimeMessage; // Import MimeMessage
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.mail.MailException; // Import MailException
import org.springframework.mail.javamail.JavaMailSender; // Import JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper; // Import MimeMessageHelper
import org.springframework.scheduling.annotation.Async; // Import Async (nếu muốn bất đồng bộ)
import org.springframework.stereotype.Service;
import com.michaelcao.bookstore_backend.entity.PasswordResetToken;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
@RequiredArgsConstructor
@Slf4j
// @EnableAsync // Bỏ comment nếu muốn bật @Async
public class EmailService {

    private final JavaMailSender mailSender; // Inject MailSender Bean

    // private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}") private String senderEmail; // Email người gửi (từ config)
    @Value("${mail.from.name}") private String senderName; // Tên người gửi (từ config)


    // @Async
    public void sendVerificationEmail(String recipientEmail, String recipientName, String verificationUrl) {
        log.info("Attempting to send verification email to {}", recipientEmail);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart, UTF-8 encoding

            helper.setFrom(senderEmail, senderName); // Đặt người gửi
            helper.setTo(recipientEmail); // Đặt người nhận
            helper.setSubject("Welcome to Our Bookstore! Please Verify Your Email"); // Đặt tiêu đề

            // Tạo nội dung HTML cho email

            String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Welcome to Our Bookstore, %s!</h2>
                    <p>Thank you for registering. Please click the link below to verify your email address:</p>
                    <p><a href="%s">Verify My Email</a></p>
                    <p>If you did not register, please ignore this email.</p>
                    <br/>
                    <p>Thanks,<br/>Michael Corp.</p>
                </body>
                </html>
                """, recipientName, verificationUrl);



            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage); // Gửi mail
            log.info("Verification email sent successfully to {}", recipientEmail);

        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            // Bắt các lỗi có thể xảy ra khi tạo hoặc gửi mail
            log.error("Failed to send verification email to {}: {}", recipientEmail, e.getMessage());
            // Ném lại exception runtime để báo hiệu lỗi nếu cần xử lý ở nơi gọi
            // Hoặc chỉ log lỗi và tiếp tục (như đã làm trong AuthServiceImpl)
            // throw new RuntimeException("Failed to send email", e);
        }
    }
    public void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetUrl) {
        log.info("Attempting to send password reset email to {}", recipientEmail);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(recipientEmail);
            helper.setSubject("Password Reset Request for Your Bookstore Account");

            // Tạo nội dung HTML
            String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>Hello %s,</p>
                    <p>We received a request to reset the password for your account. Please click the link below to set a new password:</p>
                    <p><a href="%s">Reset My Password</a></p>
                    <p>This link will expire in %d minutes.</p>
                    <p>If you did not request a password reset, please ignore this email or contact support if you have concerns.</p>
                    <br/>
                    <p>Thanks,<br/>The Bookstore Team</p>
                </body>
                </html>
                """, recipientName, resetUrl, PasswordResetToken.EXPIRATION_MINUTES); // Lấy expiration từ Entity

            // Có thể dùng Thymeleaf template tương tự như verification email

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Password reset email sent successfully to {}", recipientEmail);

        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send password reset email to {}: {}", recipientEmail, e.getMessage());
            // throw new RuntimeException("Failed to send email", e);
        }
    }
    // --- TODO: Implement sendPasswordResetEmail method ---
    // @Async
    // public void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetUrl) { ... }

}