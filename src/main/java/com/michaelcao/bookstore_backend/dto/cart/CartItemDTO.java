package com.michaelcao.bookstore_backend.dto.cart;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CartItemDTO {
    private Long cartItemId; // ID của chính dòng CartItem (hữu ích khi cập nhật/xóa)
    private Integer quantity;

    // --- Thông tin sản phẩm ---
    // Cách 1: Lồng DTO sản phẩm (ví dụ ProductSummaryDTO)
    // private ProductSummaryDTO product;

    // Cách 2: Liệt kê các trường sản phẩm cần thiết trực tiếp
    private UUID productId;
    private String productTitle;
    private String productAuthor;
    private BigDecimal productCurrentPrice; // Giá hiện tại của 1 sản phẩm (currentPrice)
    private String productCoverLink; // coverLink
    private Integer productQuantity; // quantity

    // --- Thông tin tính toán (thường được tính ở Service) ---
    private BigDecimal subtotal; // Giá tiền cho món hàng này (price * quantity)

    // Constructor (tùy chọn, hữu ích khi mapping)
    public CartItemDTO(Long cartItemId, Integer quantity, UUID productId, String productTitle, String productAuthor, BigDecimal productCurrentPrice, String productCoverLink, Integer productQuantity) {
        this.cartItemId = cartItemId;
        this.quantity = quantity;
        this.productId = productId;
        this.productTitle = productTitle;
        this.productAuthor = productAuthor;
        this.productCurrentPrice = productCurrentPrice;
        this.productCoverLink = productCoverLink;
        this.productQuantity = productQuantity;
        // Subtotal sẽ được tính và set sau trong Service
    }
}