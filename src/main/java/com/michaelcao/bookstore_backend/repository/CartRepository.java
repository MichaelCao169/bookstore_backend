package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.Cart;
import com.michaelcao.bookstore_backend.entity.User; // Import User entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import Query nếu cần custom JPQL
import org.springframework.data.repository.query.Param; // Import Param nếu cần custom JPQL
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Tìm giỏ hàng của một User cụ thể.
     * Do quan hệ là OneToOne và unique trên user_id, nên kết quả chỉ có thể là 0 hoặc 1.
     * @param user Đối tượng User.
     * @return Optional chứa Cart nếu tìm thấy.
     */
    Optional<Cart> findByUser(User user);

    /**
     * Tìm giỏ hàng bằng User ID.
     * @param userId ID của User.
     * @return Optional chứa Cart nếu tìm thấy.
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Kiểm tra xem User đã có giỏ hàng chưa.
     * @param userId ID của User.
     * @return true nếu User đã có giỏ hàng, false nếu chưa.
     */
    boolean existsByUserId(Long userId);

    /**
     * (Ví dụ về JOIN FETCH để tải Cart cùng CartItems và Product trong CartItem)
     * Lấy Cart theo User ID, đồng thời tải luôn CartItems và Product liên quan để tránh N+1 query.
     * Rất hữu ích khi cần hiển thị chi tiết giỏ hàng.
     * Lưu ý: Sử dụng LEFT JOIN FETCH để vẫn trả về Cart ngay cả khi nó không có item nào.
     *
     * @param userId ID của User.
     * @return Optional chứa Cart với các CartItems và Products đã được tải.
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product p WHERE c.user.id = :userId")
    Optional<Cart> findByUser_IdWithItemsAndProducts(@Param("userId") Long userId);

    // Bạn có thể thêm các query khác nếu cần
}