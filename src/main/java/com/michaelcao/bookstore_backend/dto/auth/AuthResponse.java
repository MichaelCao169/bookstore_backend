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
    @Builder.Default
    // Refresh token is typically sent via HttpOnly Cookie
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private List<String> roles;
}