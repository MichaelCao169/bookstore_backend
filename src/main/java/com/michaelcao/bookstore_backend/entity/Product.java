package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*; // Import các validation constraints cần thiết
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp; // Import cho tự động tạo timestamp
import org.hibernate.annotations.UpdateTimestamp;   // Import cho tự động cập nhật timestamp
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal; // Dùng BigDecimal cho tiền tệ chính xác hơn
import java.time.Instant;    // Dùng Instant (UTC) cho timestamp
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products", indexes = { // Thêm index để tăng tốc độ tìm kiếm
        @Index(name = "idx_product_title", columnList = "title"),
        @Index(name = "idx_product_author", columnList = "author"),
        @Index(name = "idx_product_category", columnList = "categoryId"),
        @Index(name = "idx_product_current_price", columnList = "current_price"), // For price-based filtering
        @Index(name = "idx_product_quantity", columnList = "quantity"), // For stock filtering
        @Index(name = "idx_product_sold_count", columnList = "sold_count"), // For bestseller sorting
        @Index(name = "idx_product_created", columnList = "created_at") // For sorting by creation date
})
@Getter
@Setter
@NoArgsConstructor
public class Product {    @Id
    @UuidGenerator
    @Column(name = "product_id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID productId;

    @NotBlank(message = "Product title cannot be blank")
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Author name cannot be blank")
    @Size(max = 150)
    @Column(nullable = false)
    private String author;

    @NotNull(message = "Original price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Original price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Original price format invalid")
    @Column(name = "original_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @NotNull(message = "Current price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Current price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Current price format invalid")
    @Column(name = "current_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "sold_count")
    private Integer soldCount = 0;

    @Min(value = 1, message = "Pages must be at least 1")
    @Column
    private Integer pages;

    @Size(max = 200)
    @Column(length = 200)
    private String publisher;

    @Column(name = "cover_link", length = 500)
    private String coverLink;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    // Quan hệ Nhiều-Một với Category: Nhiều Product thuộc về một Category
    // fetch = FetchType.LAZY: Chỉ tải Category khi cần (tốt cho hiệu năng khi lấy danh sách Product)
    //                        Tuy nhiên, khi hiển thị ProductDTO thường cần Category name,
    //                        nên LAZY có thể gây lỗi N+1 nếu không xử lý đúng (dùng JOIN FETCH hoặc @EntityGraph).
    //                        Để EAGER (@ManyToOne mặc định là EAGER) có thể tiện hơn nếu luôn cần Category.
    //                        Chúng ta sẽ bắt đầu với LAZY và xử lý N+1 sau nếu cần.
    // optional = false: Bắt buộc Product phải thuộc về một Category (không được null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId") // Tên cột foreign key trong bảng products
    private Category category;
    
    // Quan hệ Nhiều-Nhiều với Category
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    // Tự động tạo timestamp khi bản ghi được tạo
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Tự động cập nhật timestamp khi bản ghi được cập nhật
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;    // --- Constructor (tùy chọn) ---
    public Product(String title, String author, BigDecimal originalPrice, BigDecimal currentPrice, Integer quantity, Category category) {
        this.title = title;
        this.author = author;
        this.originalPrice = originalPrice;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
        this.category = category;
    }

    // --- Lưu ý về Reviews: Sẽ thêm field @OneToMany cho reviews sau ---

}