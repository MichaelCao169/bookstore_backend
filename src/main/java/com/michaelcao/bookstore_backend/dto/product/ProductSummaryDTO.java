package com.michaelcao.bookstore_backend.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// DTO này có thể hữu ích khi hiển thị danh sách sản phẩm trên trang chính
// Chỉ chứa các thông tin cần thiết để hiển thị card sản phẩm
@Data
@NoArgsConstructor
@AllArgsConstructor // Thêm AllArgsConstructor để dễ tạo trong Service (nếu dùng projection)
public class ProductSummaryDTO {
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private String imageUrl;
    private String categoryName; // Có thể thêm tên category nếu cần
}