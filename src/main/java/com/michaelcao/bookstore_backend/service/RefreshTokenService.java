
package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.entity.RefreshToken;
import com.michaelcao.bookstore_backend.entity.User;
import org.springframework.http.ResponseCookie; // For creating the cookie

import java.util.Optional;

public interface RefreshTokenService {

    // Tìm kiếm refresh token bằng chuỗi token
    Optional<RefreshToken> findByToken(String token);

    // Tạo mới một refresh token cho user
    RefreshToken createRefreshToken(Long userId);

    // Xác thực refresh token (kiểm tra hết hạn) và trả về token nếu hợp lệ
    RefreshToken verifyExpiration(RefreshToken token);

    // Xóa tất cả refresh token của một user (khi logout hoặc đổi mật khẩu)
    int deleteByUserId(Long userId);

    // Tạo HttpOnly cookie chứa refresh token
    ResponseCookie generateRefreshCookie(String token);

    // Tạo HttpOnly cookie rỗng để xóa cookie phía client
    ResponseCookie getCleanRefreshCookie();
}