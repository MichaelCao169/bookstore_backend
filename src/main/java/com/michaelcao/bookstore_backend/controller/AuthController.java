package com.michaelcao.bookstore_backend.controller;
import com.michaelcao.bookstore_backend.exception.InvalidTokenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import com.michaelcao.bookstore_backend.dto.auth.AuthResponse;
import com.michaelcao.bookstore_backend.dto.auth.LoginRequest;
import com.michaelcao.bookstore_backend.dto.auth.RegisterRequest;
import com.michaelcao.bookstore_backend.entity.RefreshToken;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.exception.TokenRefreshException;
import com.michaelcao.bookstore_backend.security.jwt.JwtUtil;
import com.michaelcao.bookstore_backend.service.AuthService;
import com.michaelcao.bookstore_backend.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping; // *** IMPORT THÊM ***
import org.springframework.web.bind.annotation.RequestParam; // *** IMPORT THÊM ***
import com.michaelcao.bookstore_backend.dto.auth.ForgotPasswordRequest; // *** TẠO DTO NÀY ***
import com.michaelcao.bookstore_backend.dto.auth.ResetPasswordRequest; // *** TẠO DTO NÀY ***
import jakarta.validation.Valid; // Đảm bảo đã import
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j // Added for logging
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService; // Inject RefreshTokenService
    private final JwtUtil jwtUtil;                        // Inject JwtUtil
    @Value("${app.jwt.refresh-cookie-name}")              // Inject cookie name
    private String refreshTokenCookieName;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Received registration request for email: {}", registerRequest.getEmail());
        // Exceptions are handled by GlobalExceptionHandler now
        return authService.register(registerRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received login request for email: {}", loginRequest.getEmail());
        // Exceptions are handled by GlobalExceptionHandler or within the service
        return authService.login(loginRequest);
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String extractedTokenValue = null; // Đổi tên biến tạm thời
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (refreshTokenCookieName.equals(cookie.getName())) {
                    extractedTokenValue = cookie.getValue();
                    break;
                }
            }
        }

        // *** TẠO BIẾN FINAL TRƯỚC KHI DÙNG TRONG LAMBDA ***
        final String finalRefreshTokenValue = extractedTokenValue;

        if (!StringUtils.hasText(finalRefreshTokenValue)) {
            log.warn("Refresh token is missing in the cookie.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Refresh token is missing.");
        }

        log.debug("Received refresh token request with token value (from cookie)");

        try {
            // Tìm và xác thực refresh token (sử dụng biến final)
            RefreshToken refreshToken = refreshTokenService.findByToken(finalRefreshTokenValue)
                    .map(refreshTokenService::verifyExpiration)
                    // *** SỬ DỤNG BIẾN FINAL TRONG LAMBDA ***
                    .orElseThrow(() -> new TokenRefreshException(finalRefreshTokenValue, "Refresh token not found in database!"));

            User user = refreshToken.getUser();
            String newAccessToken = jwtUtil.generateToken(user);
            log.info("Generated new access token for user: {}", user.getEmail());

            // (Optional: Refresh Token Rotation logic here...)

            AuthResponse response = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .roles(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .build();

            // (Return response, potentially with new refresh token cookie if using rotation...)
            return ResponseEntity.ok(response);

        } catch (TokenRefreshException ex) {
            log.warn("Refresh token error: {}", ex.getMessage());
            ResponseCookie cleanCookie = refreshTokenService.getCleanRefreshCookie();
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                    .body(ex.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
            ResponseCookie cleanCookie = refreshTokenService.getCleanRefreshCookie();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                    .body("Internal server error during token refresh.");
        }
    }

    // ... (logout method - kiểm tra xem có lỗi tương tự không, nếu có thì áp dụng cách tạo biến final tương tự cho username/refreshTokenValue) ...
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Tạo biến final cho username
        final String finalUsername = (authentication != null && authentication.getPrincipal() instanceof UserDetails)
                ? ((UserDetails) authentication.getPrincipal()).getUsername()
                : "UNKNOWN";

        String extractedTokenValue = null; // Biến tạm
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (refreshTokenCookieName.equals(cookie.getName())) {
                    extractedTokenValue = cookie.getValue();
                    break;
                }
            }
        }
        // Tạo biến final cho token
        final String finalRefreshTokenValue = extractedTokenValue;


        if (StringUtils.hasText(finalRefreshTokenValue)) {
            // Sử dụng biến final trong lambda (nếu cần) hoặc bên ngoài lambda
            refreshTokenService.findByToken(finalRefreshTokenValue).ifPresent(refreshToken -> {
                // Lambda này dùng refreshToken (param) và finalUsername (effectively final)
                refreshTokenService.deleteByUserId(refreshToken.getUser().getId());
                log.info("Refresh token deleted for user: {}", finalUsername);
            });

        } else {
            log.warn("Logout request received, but no refresh token found in cookie for user: {}", finalUsername);
        }

        SecurityContextHolder.clearContext();
        ResponseCookie cleanRefreshCookie = refreshTokenService.getCleanRefreshCookie();

        log.info("User {} logged out successfully.", finalUsername);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanRefreshCookie.toString())
                .body("Logout successful!");
    }
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) { // Nhận token từ query param
        log.info("Received email verification request with token: {}", token);
        try {
            authService.verifyAccount(token); // Gọi service để xử lý việc xác thực
            // TODO: Redirect đến trang thông báo thành công của Frontend thay vì trả về text
            // return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("http://localhost:3000/email-verified")).build();
            return ResponseEntity.ok("Email verified successfully! You can now login.");
        } catch (Exception e) {
            log.error("Email verification failed for token {}: {}", token, e.getMessage());
            // TODO: Redirect đến trang thông báo lỗi của Frontend
            // return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("http://localhost:3000/email-verification-failed")).build();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage()); // Trả về lỗi
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        log.info("Received forgot password request for email: {}", forgotPasswordRequest.getEmail());
        try {
            authService.forgotPassword(forgotPasswordRequest.getEmail());
            // Luôn trả về thông báo thành công chung chung để tránh lộ email
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
        } catch (Exception e) {
            // Log lỗi thực tế nhưng vẫn trả về thông báo chung
            log.error("Error during forgot password request for email {}: {}", forgotPasswordRequest.getEmail(), e.getMessage());
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
            // Hoặc nếu muốn báo lỗi rõ ràng hơn khi có lỗi nghiêm trọng (ví dụ không gửi được mail)
            // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        log.info("Received password reset request for token: {}", resetPasswordRequest.getToken());
        try {
            authService.resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword());
            return ResponseEntity.ok("Password has been reset successfully. You can now login with your new password.");
        } catch (InvalidTokenException | IllegalArgumentException e) { // Bắt lỗi token hoặc mật khẩu không hợp lệ
            log.warn("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error during password reset for token {}: {}", resetPasswordRequest.getToken(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred while resetting password.");
        }
    }

    // --- TODO: Add endpoints for /verify-email, /forgot-password, /reset-password, /refresh ---

}