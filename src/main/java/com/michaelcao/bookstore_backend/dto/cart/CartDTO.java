package com.michaelcao.bookstore_backend.dto.cart;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList; 
import java.util.List;   

@Data
@NoArgsConstructor
public class CartDTO {
    private Long cartId; // ID của giỏ hàng
    private List<CartItemDTO> items = new ArrayList<>(); // Danh sách các món hàng trong giỏ
    private BigDecimal totalPrice = BigDecimal.ZERO; // Tổng giá trị giỏ hàng
    private int totalItems = 0; // Tổng số lượng các món hàng 

    // Constructor 
    public CartDTO(Long cartId) {
        this.cartId = cartId;
    }
}