package com.michaelcao.bookstore_backend.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateProductRequest {
    @NotBlank(message = "Product title cannot be blank")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Author name cannot be blank")
    @Size(max = 150)
    private String author;

    @Size(max = 20, message = "ISBN cannot exceed 20 characters")
    private String isbn; // Có thể là null

    private String description; // Có thể là null

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Price format invalid (max 10 integer, 2 fraction digits)")
    private BigDecimal price;

    @NotNull(message = "Stock quantity cannot be null")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl; // Có thể là null

    private LocalDate publishedDate; // Có thể là null

    @NotNull(message = "Category ID cannot be null")
    @Min(value = 1, message = "Category ID must be positive") // ID thường bắt đầu từ 1
    private Long categoryId; // Chỉ cần ID của category khi tạo
}