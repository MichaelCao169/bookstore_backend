package com.michaelcao.bookstore_backend.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductRequest {
    @NotBlank(message = "Product title cannot be blank")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Author name cannot be blank")
    @Size(max = 150)
    private String author;

    @NotNull(message = "Original price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal originalPrice;

    @NotNull(message = "Current price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal currentPrice;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0)
    private Integer quantity;

    @Min(value = 1, message = "Pages must be at least 1")
    private Integer pages;

    @Size(max = 200)
    private String publisher;

    @Size(max = 500)
    private String coverLink;

    private String description;

    @NotNull(message = "Category ID cannot be null")
    @Min(value = 1)
    private Long categoryId;
    
    private List<Long> categoryIds;
}