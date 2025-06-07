package com.michaelcao.bookstore_backend.dto.product;

import com.michaelcao.bookstore_backend.dto.category.CategoryDTO; // Import CategoryDTO
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private UUID productId;
    private String title;
    private String author;
    private BigDecimal originalPrice;
    private BigDecimal currentPrice;
    private Integer quantity;
    private Integer soldCount;
    private Integer pages;
    private String publisher;
    private String coverLink;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private CategoryDTO category;
    private List<CategoryDTO> categories; // Danh sách các danh mục của sản phẩm
    
    @Builder.Default
    private Double averageRating = 0.0; // Điểm trung bình (mặc định là 0 nếu chưa có)
      @Builder.Default
    private Long reviewCount = 0L;     // Số lượng đánh giá (mặc định là 0)
}