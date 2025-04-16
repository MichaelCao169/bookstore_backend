package com.michaelcao.bookstore_backend.dto.review;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment; // Comment có thể là null

    // productId sẽ được lấy từ URL path, không cần gửi trong body
    // userId sẽ được lấy từ SecurityContext
}