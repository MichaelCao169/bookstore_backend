package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min; // Import Min constraint
import jakarta.validation.constraints.NotNull; // Import NotNull constraint
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart_items", indexes = { // Index trên cart_id và product_id để tìm kiếm nhanh
        @Index(name = "idx_cartitem_cart", columnList = "cart_id"),
        @Index(name = "idx_cartitem_product", columnList = "product_id")
}, uniqueConstraints = {
        // Đảm bảo không có 2 dòng CartItem cho cùng 1 Product trong cùng 1 Cart
        @UniqueConstraint(columnNames = {"cart_id", "product_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ Nhiều-Một với Cart: Nhiều CartItem thuộc về một Cart
    // optional = false: Một CartItem phải thuộc về một Cart.
    @NotNull // Validation: Không được null khi lưu
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false) // Khóa ngoại trỏ đến carts.id
    private Cart cart;

    // Quan hệ Nhiều-Một với Product: Nhiều CartItem có thể trỏ đến cùng một Product
    // (Mỗi dòng CartItem ứng với 1 sản phẩm trong 1 giỏ hàng cụ thể)
    // optional = false: Một CartItem phải tương ứng với một Product.
    @NotNull // Validation
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // LAZY thường tốt hơn ở đây
    @JoinColumn(name = "product_id", nullable = false) // Khóa ngoại trỏ đến products.id
    private Product product;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1") // Số lượng phải ít nhất là 1
    @Column(nullable = false)
    private Integer quantity;

    // Constructor (tùy chọn)
    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    // Có thể thêm phương thức tính giá tiền cho item này
    // public BigDecimal getSubtotal() {
    //     if (product != null && product.getPrice() != null && quantity != null) {
    //         return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    //     }
    //     return BigDecimal.ZERO;
    // }
}