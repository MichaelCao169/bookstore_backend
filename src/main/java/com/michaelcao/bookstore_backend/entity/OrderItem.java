package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_orderitem_order", columnList = "order_id"),
        @Index(name = "idx_orderitem_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    // Quan hệ Nhiều-Một với Order
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Quan hệ Nhiều-Một với Product
    // Lưu ý: Không nên để cascade ở đây. Việc xóa Product không nên tự động xóa OrderItem.
    // Nếu Product bị xóa, ta cần xử lý logic riêng (ví dụ: không cho xóa Product nếu còn trong OrderItem,
    // hoặc đánh dấu OrderItem là không hợp lệ).
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Liên kết đến sản phẩm đã mua

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity; // Số lượng sản phẩm này trong đơn hàng

    @NotNull(message = "Price at purchase cannot be null")
    @Column(name = "price_at_purchase", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtPurchase; // *** Quan trọng: Lưu lại giá sản phẩm TẠI THỜI ĐIỂM đặt hàng ***
    // Tránh trường hợp giá sản phẩm thay đổi sau này ảnh hưởng đến đơn hàng cũ.

    // Constructor (tùy chọn)
    public OrderItem(Order order, Product product, Integer quantity, BigDecimal priceAtPurchase) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }
}