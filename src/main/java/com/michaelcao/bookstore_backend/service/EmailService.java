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
            helper.setSubject("Chào mừng đến với AtomicBooks! Xác thực tài khoản của bạn"); // Đặt tiêu đề

            // Tạo nội dung HTML cho email
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #f0f0f0; border-radius: 5px;">
                        <div style="text-align: center; margin-bottom: 20px;">
                            <h1 style="color: #f97316;">AtomicBooks</h1>
                        </div>
                        <h2 style="color: #4a5568;">Chào mừng đến với AtomicBooks, %s!</h2>
                        <p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng nhấp vào nút bên dưới để xác thực địa chỉ email của bạn:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #f97316; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold;">Xác thực tài khoản</a>
                        </div>
                        <p>Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.</p>
                        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #f0f0f0;">
                            <p>Trân trọng,<br/>Đội ngũ AtomicBooks</p>
                        </div>
                    </div>
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
            helper.setSubject("Yêu cầu đặt lại mật khẩu tài khoản AtomicBooks");

            // Tạo nội dung HTML tiếng Việt
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #f0f0f0; border-radius: 5px;">
                        <div style="text-align: center; margin-bottom: 20px;">
                            <h1 style="color: #f97316;">AtomicBooks</h1>
                        </div>
                        <h2 style="color: #4a5568;">Yêu cầu đặt lại mật khẩu</h2>
                        <p>Xin chào %s,</p>
                        <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Vui lòng nhấp vào nút bên dưới để đặt mật khẩu mới:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #f97316; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold;">Đặt lại mật khẩu</a>
                        </div>
                        <p>Liên kết này sẽ hết hạn sau %d phút.</p>
                        <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này hoặc liên hệ với chúng tôi nếu bạn có bất kỳ thắc mắc.</p>
                        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #f0f0f0;">
                            <p>Trân trọng,<br/>Đội ngũ AtomicBooks</p>
                        </div>
                    </div>
                </body>
                </html>
                """, recipientName, resetUrl, PasswordResetToken.EXPIRATION_MINUTES);

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