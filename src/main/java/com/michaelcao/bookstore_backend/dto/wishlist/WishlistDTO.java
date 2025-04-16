package com.michaelcao.bookstore_backend.dto.wishlist;

import com.michaelcao.bookstore_backend.dto.product.ProductSummaryDTO; // Dùng DTO tóm tắt sản phẩm
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class WishlistDTO {
    // Không cần ID cho Wishlist vì nó gắn liền với User
    private List<ProductSummaryDTO> items = new ArrayList<>(); // Danh sách các sản phẩm yêu thích
    private int itemCount; // Số lượng sản phẩm yêu thích

    public WishlistDTO(List<ProductSummaryDTO> items) {
        this.items = items;
        this.itemCount = (items != null) ? items.size() : 0;
    }
}