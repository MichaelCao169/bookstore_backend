package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // Import validation constraint
import jakarta.validation.constraints.Size;   // Import validation constraint
import lombok.Getter; // Dùng Getter/Setter thay cho @Data để tránh lỗi hashCode/equals với relationship
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet; // Import HashSet
import java.util.Set;     // Import Set

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name") // Đảm bảo tên danh mục là duy nhất
})
@Getter // Lombok: Chỉ tạo Getters
@Setter // Lombok: Chỉ tạo Setters
@NoArgsConstructor // Lombok: Constructor không tham số (cần cho JPA)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name cannot be blank") // Validation
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters") // Validation
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500) // Cho phép mô tả dài hơn một chút
    private String description;

    // Quan hệ Một-Nhiều với Product: Một Category có nhiều Product
    // mappedBy = "category": Chỉ định rằng quan hệ này được quản lý bởi field "category" trong entity Product.
    // cascade = CascadeType.ALL: Khi xóa Category, các Product thuộc Category đó cũng bị xóa (Cẩn thận khi dùng!).
    //                          Có thể dùng CascadeType.PERSIST, CascadeType.MERGE nếu chỉ muốn cascade lưu/update.
    //                          Hoặc không dùng cascade và xử lý thủ công (ví dụ: không cho xóa Category nếu còn Product).
    // fetch = FetchType.LAZY: Chỉ tải danh sách products khi được gọi đến (tốt cho hiệu năng).
    @OneToMany(mappedBy = "category", cascade = CascadeType.DETACH, fetch = FetchType.LAZY, orphanRemoval = false)
    // orphanRemoval=false: Khi xóa product khỏi collection này, nó không tự động bị xóa khỏi DB.
    // Dùng Set để tránh trùng lặp Product trong danh sách (mặc dù ít khi xảy ra ở đây).
    private Set<Product> products = new HashSet<>();

    // Constructor với tham số (tùy chọn, hữu ích khi tạo đối tượng)
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Helper methods (tùy chọn) để quản lý quan hệ hai chiều
    public void addProduct(Product product) {
        this.products.add(product);
        product.setCategory(this);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
        product.setCategory(null);
    }
}