package com.michaelcao.bookstore_backend.dto.order;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class OrderItemDTO {
    private Long orderItemId; // ID của OrderItem
    private Long productId;
    private String productTitle;
    private String productAuthor; // Thêm author cho dễ nhìn
    private String productImageUrl; // Thêm ảnh
    private Integer quantity;
    private BigDecimal priceAtPurchase; // Giá tại thời điểm mua
    private BigDecimal subtotal; // Tổng tiền cho item này (quantity * priceAtPurchase)

    // Constructor (tùy chọn)
    public OrderItemDTO(Long orderItemId, Long productId, String productTitle, String productAuthor, String productImageUrl, Integer quantity, BigDecimal priceAtPurchase) {
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productTitle = productTitle;
        this.productAuthor = productAuthor;
        this.productImageUrl = productImageUrl;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
        if (priceAtPurchase != null && quantity != null) {
            this.subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
}