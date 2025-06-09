package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.entity.RefreshToken;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.exception.TokenRefreshException;
import com.michaelcao.bookstore_backend.repository.RefreshTokenRepository;
import com.michaelcao.bookstore_backend.repository.UserRepository;
import com.michaelcao.bookstore_backend.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh-token-expiration-ms}")
    private Long refreshTokenDurationMs;

    // Tên của cookie chứa refresh token
    @Value("${app.jwt.refresh-cookie-name}") // Thêm property này vào application.properties
    private String refreshTokenCookieName;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional // Cần transaction vì có thao tác với DB
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("Error: User not found for Refresh Token creation with ID: " + userId)
        );

        // Xóa các token cũ của user này nếu muốn mỗi user chỉ có 1 refresh token tại 1 thời điểm
        // refreshTokenRepository.deleteByUser(user); // Bỏ comment nếu muốn chính sách này

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .token(UUID.randomUUID().toString()) // Tạo chuỗi token ngẫu nhiên
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token); // Xóa token hết hạn khỏi DB
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Override
    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("Error: User not found for Refresh Token deletion with ID: " + userId)
        );
        return refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public ResponseCookie generateRefreshCookie(String token) {
        // Tạo HttpOnly cookie, an toàn hơn LocalStorage
        return ResponseCookie.from(refreshTokenCookieName, token)
                .path("/") // Cookie available for all paths
                .maxAge(Duration.ofMillis(refreshTokenDurationMs)) // Thời gian sống của cookie = thời gian sống token
                .httpOnly(true)   // Quan trọng: Ngăn JS truy cập cookie
                .secure(false)    // TODO: Set true nếu dùng HTTPS trong production
                .sameSite("Lax") // Hoặc "Strict" - Giúp chống CSRF. "None" nếu FE/BE khác site hoàn toàn và dùng HTTPS
                .build();
    }

    @Override
    public ResponseCookie getCleanRefreshCookie() {
        // Tạo cookie rỗng với maxAge=0 để xóa cookie hiện có
        return ResponseCookie.from(refreshTokenCookieName, "") // Giá trị rỗng
                .path("/")
                .maxAge(0) // Hết hạn ngay lập tức
                .httpOnly(true)
                .secure(false) // Phải khớp với secure flag khi tạo
                .sameSite("Lax") // Phải khớp với sameSite flag khi tạo
                .build();
    }
}