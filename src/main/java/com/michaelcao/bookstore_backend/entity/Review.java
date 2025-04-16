package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*; // Import các constraints
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_product", columnList = "product_id"), // Index theo sản phẩm
        @Index(name = "idx_review_user", columnList = "user_id")       // Index theo người dùng
}, uniqueConstraints = {
        // Đảm bảo mỗi user chỉ đánh giá một sản phẩm một lần
        @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(nullable = false)
    private Integer rating; // Số sao đánh giá (1-5)

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    @Column(length = 1000) // Cho phép comment dài
    private String comment; // Nội dung bình luận (có thể null nếu chỉ muốn rating)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Quan hệ Nhiều-Một với User: Nhiều Review có thể của một User
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // LAZY để không load user khi chỉ lấy review list
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Quan hệ Nhiều-Một với Product: Nhiều Review thuộc về một Product
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Constructor (tùy chọn)
    public Review(Integer rating, String comment, User user, Product product) {
        this.rating = rating;
        this.comment = comment;
        this.user = user;
        this.product = product;
    }
}