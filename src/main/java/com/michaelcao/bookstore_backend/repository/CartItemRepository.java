package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.Cart; // Import Cart
import com.michaelcao.bookstore_backend.entity.CartItem;
import com.michaelcao.bookstore_backend.entity.Product; // Import Product
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Import Modifying for DELETE/UPDATE queries
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set; // Import Set

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> { // Entity: CartItem, ID: Long

    /**
     * Tìm một CartItem cụ thể trong một Cart dựa vào Product.
     * Hữu ích để kiểm tra xem sản phẩm đã có trong giỏ chưa.
     * @param cart Đối tượng Cart.
     * @param product Đối tượng Product.
     * @return Optional chứa CartItem nếu tìm thấy.
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    /**
     * Tìm một CartItem cụ thể dựa vào Cart ID và Product ID.
     * @param cartId ID của Cart.
     * @param productId ID của Product.
     * @return Optional chứa CartItem nếu tìm thấy.
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    /**
     * Tìm tất cả các CartItem thuộc về một Cart.
     * (Thường không cần thiết vì có thể lấy qua cart.getCartItems(),
     * nhưng có thể hữu ích trong một số trường hợp)
     * @param cart Đối tượng Cart.
     * @return Set các CartItem.
     */
    Set<CartItem> findByCart(Cart cart);

    /**
     * Xóa một CartItem dựa vào ID và Cart ID (tăng cường bảo mật, đảm bảo user chỉ xóa item trong giỏ của họ).
     * @param cartItemId ID của CartItem cần xóa.
     * @param cartId ID của Cart chứa item đó.
     */
    @Modifying // Cần thiết cho các query thay đổi dữ liệu (DELETE, UPDATE)
    @Query("DELETE FROM CartItem ci WHERE ci.id = :cartItemId AND ci.cart.id = :cartId")
    void deleteByIdAndCartId(@Param("cartItemId") Long cartItemId, @Param("cartId") Long cartId);

    /**
     * Xóa tất cả các CartItem thuộc về một Cart ID.
     * Hữu ích khi cần xóa sạch giỏ hàng (ví dụ sau khi checkout).
     * @param cartId ID của Cart.
     */
    @Modifying
    void deleteByCartId(Long cartId);

}