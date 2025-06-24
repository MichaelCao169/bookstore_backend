package com.michaelcao.bookstore_backend.dto.cart;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CartItemDTO {
    private Long cartItemId; 
    private Integer quantity;

    // Thông tin sản phẩm
    private UUID productId;
    private String productTitle;
    private String productAuthor;
    private BigDecimal productCurrentPrice; // Giá hiện tại của 1 sản phẩm (currentPrice)
    private String productCoverLink; // coverLink
    private Integer productQuantity; // quantity

    
    private BigDecimal subtotal; // Giá tiền cho món hàng này (price * quantity)

    // Constructor 
    public CartItemDTO(Long cartItemId, Integer quantity, UUID productId, String productTitle, String productAuthor, BigDecimal productCurrentPrice, String productCoverLink, Integer productQuantity) {
        this.cartItemId = cartItemId;
        this.quantity = quantity;
        this.productId = productId;
        this.productTitle = productTitle;
        this.productAuthor = productAuthor;
        this.productCurrentPrice = productCurrentPrice;
        this.productCoverLink = productCoverLink;
        this.productQuantity = productQuantity;
        
    }
}