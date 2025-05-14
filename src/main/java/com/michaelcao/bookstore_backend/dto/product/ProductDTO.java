package com.michaelcao.bookstore_backend.dto.product;

import com.michaelcao.bookstore_backend.dto.category.CategoryDTO; // Import CategoryDTO
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private UUID id;
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
    private List<CategoryDTO> categories; // Danh sách các danh mục của sản phẩm
    
    @Builder.Default
    private Double averageRating = 0.0; // Điểm trung bình (mặc định là 0 nếu chưa có)
    
    @Builder.Default
    private Long reviewCount = 0L;     // Số lượng đánh giá (mặc định là 0)
    
    @Builder.Default
    private Integer soldCount = 0;    // Số lượng sản phẩm đã bán
}