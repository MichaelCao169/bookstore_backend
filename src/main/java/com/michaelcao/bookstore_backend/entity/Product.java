package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*; // Import các validation constraints cần thiết
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp; // Import cho tự động tạo timestamp
import org.hibernate.annotations.UpdateTimestamp;   // Import cho tự động cập nhật timestamp

import java.math.BigDecimal; // Dùng BigDecimal cho tiền tệ chính xác hơn
import java.time.Instant;    // Dùng Instant (UTC) cho timestamp
import java.time.LocalDate;  // Dùng LocalDate cho ngày xuất bản

@Entity
@Table(name = "products", indexes = { // Thêm index để tăng tốc độ tìm kiếm
        @Index(name = "idx_product_title", columnList = "title"),
        @Index(name = "idx_product_author", columnList = "author"),
        @Index(name = "idx_product_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product title cannot be blank")
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Author name cannot be blank")
    @Size(max = 150)
    @Column(nullable = false)
    private String author;

    @Size(max = 20) // ISBN có thể có 10 hoặc 13 ký tự, có thể có dấu gạch nối
    @Column(length = 20, unique = true) // ISBN nên là duy nhất (hoặc không?)
    private String isbn;

    @Lob // Đánh dấu là Large Object, có thể map tới kiểu TEXT hoặc CLOB trong DB
    @Column(columnDefinition = "TEXT") // Chỉ định rõ kiểu cột trong DB nếu cần
    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive") // Giá phải lớn hơn 0
    @Digits(integer = 10, fraction = 2, message = "Price format invalid") // Tối đa 10 chữ số phần nguyên, 2 chữ số phần thập phân
    @Column(nullable = false, precision = 12, scale = 2) // precision = tổng số chữ số, scale = số chữ số sau dấu phẩy
    private BigDecimal price;

    @NotNull(message = "Stock quantity cannot be null")
    @Min(value = 0, message = "Stock quantity cannot be negative") // Số lượng không được âm
    @Column(nullable = false)
    private Integer stockQuantity = 0; // Giá trị mặc định

    @Column(length = 500) // URL ảnh có thể dài
    private String imageUrl;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    // Quan hệ Nhiều-Một với Category: Nhiều Product thuộc về một Category
    // fetch = FetchType.LAZY: Chỉ tải Category khi cần (tốt cho hiệu năng khi lấy danh sách Product)
    //                        Tuy nhiên, khi hiển thị ProductDTO thường cần Category name,
    //                        nên LAZY có thể gây lỗi N+1 nếu không xử lý đúng (dùng JOIN FETCH hoặc @EntityGraph).
    //                        Để EAGER (@ManyToOne mặc định là EAGER) có thể tiện hơn nếu luôn cần Category.
    //                        Chúng ta sẽ bắt đầu với LAZY và xử lý N+1 sau nếu cần.
    // optional = false: Bắt buộc Product phải thuộc về một Category (không được null)
    @NotNull(message = "Category cannot be null") // Validation
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false) // Tên cột foreign key trong bảng products
    private Category category;

    // Tự động tạo timestamp khi bản ghi được tạo
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Tự động cập nhật timestamp khi bản ghi được cập nhật
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // --- Constructor (tùy chọn) ---
    public Product(String title, String author, BigDecimal price, Integer stockQuantity, Category category) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

    // --- Lưu ý về Reviews: Sẽ thêm field @OneToMany cho reviews sau ---

}