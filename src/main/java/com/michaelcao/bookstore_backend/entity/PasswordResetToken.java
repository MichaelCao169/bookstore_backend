// src/main/java/com/michaelcao/bookstore_backend/entity/PasswordResetToken.java
package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit; // For adding minutes

@Data
@NoArgsConstructor
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    // Thời gian hết hạn ngắn hơn (ví dụ: 60 phút)
    public static final int EXPIRATION_MINUTES = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION_MINUTES);
    }

    private Instant calculateExpiryDate(int expiryTimeInMinutes) {
        return Instant.now().plus(expiryTimeInMinutes, ChronoUnit.MINUTES);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
}