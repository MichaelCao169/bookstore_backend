package com.michaelcao.bookstore_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken; 
    @Builder.Default
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String name;
    private String avatarUrl;
    private List<String> roles;
    
    // Kiểm tra xem email của user đã được xác thực chưa
    @Builder.Default
    private Boolean verified = true;
    
    // Thông báo tùy chọn cho client
    private String message;
}