package com.michaelcao.bookstore_backend.dto.product;

import com.michaelcao.bookstore_backend.dto.category.CategoryDTO; // Import CategoryDTO
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
@Data
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private LocalDate publishedDate;
    private Instant createdAt;
    private Instant updatedAt;
    private CategoryDTO category;
    private Double averageRating = 0.0; // Điểm trung bình (mặc định là 0 nếu chưa có)
    private Long reviewCount = 0L;     // Số lượng đánh giá (mặc định là 0)
}