package com.michaelcao.bookstore_backend.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateProductRequest {
    // Validation tương tự CreateProductRequest
    @NotBlank(message = "Product title cannot be blank")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Author name cannot be blank")
    @Size(max = 150)
    private String author;

    @Size(max = 20, message = "ISBN cannot exceed 20 characters")
    private String isbn;

    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @NotNull(message = "Stock quantity cannot be null")
    @Min(value = 0)
    private Integer stockQuantity;

    @Size(max = 500)
    private String imageUrl;

    private LocalDate publishedDate;

    @NotNull(message = "Category ID cannot be null")
    @Min(value = 1)
    private Long categoryId;
    
    private List<Long> categoryIds;
}