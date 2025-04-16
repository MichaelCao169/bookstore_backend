package com.michaelcao.bookstore_backend.dto.review;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class ReviewDTO {
    private Long reviewId; // ID của đánh giá
    private Integer rating;
    private String comment;
    private Instant createdAt;

    // --- Thông tin người đánh giá ---
    // Chỉ hiển thị những thông tin public, không nhạy cảm
    private Long userId;
    private String userName; // Tên người đánh giá

    // --- (Tùy chọn) Thông tin sản phẩm (Thường không cần khi hiển thị list review của 1 sản phẩm) ---
    // private Long productId;
    // private String productTitle;

    // Constructor (hữu ích khi mapping)
    public ReviewDTO(Long reviewId, Integer rating, String comment, Instant createdAt, Long userId, String userName) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.userId = userId;
        this.userName = userName;
    }
}