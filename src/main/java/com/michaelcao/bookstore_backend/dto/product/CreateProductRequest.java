package com.michaelcao.bookstore_backend.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {
    @NotBlank(message = "Product title cannot be blank")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Author name cannot be blank")
    @Size(max = 150)
    private String author;

    @NotNull(message = "Original price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Original price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Original price format invalid")
    private BigDecimal originalPrice;

    @NotNull(message = "Current price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Current price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Current price format invalid")
    private BigDecimal currentPrice;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Min(value = 1, message = "Pages must be at least 1")
    private Integer pages;

    @Size(max = 200, message = "Publisher name cannot exceed 200 characters")
    private String publisher;

    @Size(max = 500, message = "Cover link cannot exceed 500 characters")
    private String coverLink;

    private String description;

    @NotNull(message = "Category ID cannot be null")
    @Min(value = 1, message = "Category ID must be positive")
    private Long categoryId;
    
    private List<Long> categoryIds;
}