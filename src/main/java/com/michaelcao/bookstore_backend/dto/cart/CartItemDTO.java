package com.michaelcao.bookstore_backend.dto.cart;

// Import các DTO hoặc trường cần thiết từ Product
import com.michaelcao.bookstore_backend.dto.product.ProductSummaryDTO; // Hoặc dùng ProductDTO nếu cần nhiều chi tiết hơn
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CartItemDTO {
    private Long cartItemId; // ID của chính dòng CartItem (hữu ích khi cập nhật/xóa)
    private Integer quantity;

    // --- Thông tin sản phẩm ---
    // Cách 1: Lồng DTO sản phẩm (ví dụ ProductSummaryDTO)
    // private ProductSummaryDTO product;

    // Cách 2: Liệt kê các trường sản phẩm cần thiết trực tiếp
    private Long productId;
    private String productTitle;
    private String productAuthor;
    private BigDecimal productPrice; // Giá của 1 sản phẩm
    private String productImageUrl;
    private Integer productStockQuantity; // Hiển thị tồn kho để người dùng biết

    // --- Thông tin tính toán (thường được tính ở Service) ---
    private BigDecimal subtotal; // Giá tiền cho món hàng này (price * quantity)

    // Constructor (tùy chọn, hữu ích khi mapping)
    public CartItemDTO(Long cartItemId, Integer quantity, Long productId, String productTitle, String productAuthor, BigDecimal productPrice, String productImageUrl, Integer productStockQuantity) {
        this.cartItemId = cartItemId;
        this.quantity = quantity;
        this.productId = productId;
        this.productTitle = productTitle;
        this.productAuthor = productAuthor;
        this.productPrice = productPrice;
        this.productImageUrl = productImageUrl;
        this.productStockQuantity = productStockQuantity;
        // Subtotal sẽ được tính và set sau trong Service
    }
}