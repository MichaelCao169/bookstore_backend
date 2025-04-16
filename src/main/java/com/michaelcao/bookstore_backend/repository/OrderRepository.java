package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.Order;
import com.michaelcao.bookstore_backend.entity.OrderStatus; // Import Enum nếu cần dùng trong query
import com.michaelcao.bookstore_backend.entity.User;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import Query
import org.springframework.data.repository.query.Param; // Import Param
import org.springframework.stereotype.Repository;

import java.util.Optional; // Import Optional

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> { // Entity: Order, ID: Long



    /**
     * Tìm các đơn hàng của một User ID cụ thể, sắp xếp theo ngày đặt hàng giảm dần.
     * @param userId ID của User.
     * @param pageable Thông tin phân trang.
     * @return Page chứa danh sách Order.
     */
    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);

    /**
     * Tìm một đơn hàng cụ thể bằng ID VÀ User ID (để đảm bảo user chỉ xem được đơn hàng của mình).
     * @param orderId ID của Order.
     * @param userId ID của User.
     * @return Optional chứa Order nếu tìm thấy và khớp user.
     */
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    /**
     * (Ví dụ JOIN FETCH)
     * Tìm một đơn hàng cụ thể bằng ID, đồng thời tải luôn User và OrderItems (và Product trong OrderItems).
     * Hữu ích khi cần hiển thị chi tiết đơn hàng.
     * @param orderId ID của Order.
     * @return Optional chứa Order với các thông tin liên quan đã được tải.
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.user u " +          // Tải User
            "LEFT JOIN FETCH o.orderItems oi " +   // Tải OrderItems
            "LEFT JOIN FETCH oi.product p " +      // Tải Product trong từng OrderItem
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Long orderId);


    /**
     * (Ví dụ JOIN FETCH cho Admin)
     * Tìm tất cả đơn hàng (dành cho Admin), có phân trang, đồng thời tải luôn User.
     * Giúp hiển thị tên user trong danh sách đơn hàng của Admin mà không cần query N+1.
     * @param pageable Thông tin phân trang.
     * @return Page chứa danh sách Order với thông tin User đã được tải.
     */
    @Query(value = "SELECT o FROM Order o LEFT JOIN FETCH o.user u",
            countQuery = "SELECT count(o) FROM Order o") // Cần countQuery riêng khi có FETCH trong query chính của Page
    Page<Order> findAllWithUser(Pageable pageable);

    /**
     * Tìm các đơn hàng của một User ID cụ thể, SẮP XẾP theo ngày giảm dần,
     * ĐỒNG THỜI TẢI LUÔN thông tin User để tránh N+1 khi map sang DTO.
     * @param userId ID của User.
     * @param pageable Thông tin phân trang.
     * @return Page chứa danh sách Order với thông tin User đã được tải.
     */
    @Query(value = "SELECT o FROM Order o LEFT JOIN FETCH o.user u WHERE u.id = :userId", // Thêm JOIN FETCH o.user u và WHERE u.id
            countQuery = "SELECT count(o) FROM Order o WHERE o.user.id = :userId") // Count query không cần FETCH
    Page<Order> findByUserIdWithUserOrderByOrderDateDesc(@Param("userId") Long userId, Pageable pageable);


    // Bạn có thể thêm các phương thức khác dựa trên nhu cầu
    // Ví dụ: tìm đơn hàng theo trạng thái
    // Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Kiểm tra xem có tồn tại ít nhất một đơn hàng của user
     * đã được giao thành công (DELIVERED) và chứa sản phẩm với productId cho trước không.
     * @param userId ID của User.
     * @param productId ID của Product.
     * @return true nếu tồn tại đơn hàng thỏa mãn, false nếu không.
     */
    @Query("SELECT COUNT(o.id) > 0 FROM Order o JOIN o.orderItems oi " +
            "WHERE o.user.id = :userId AND oi.product.id = :productId AND o.status = :status")
    boolean existsByUserIdAndItemsProductIdAndStatusDelivered(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("status") OrderStatus status // Truyền vào OrderStatus.DELIVERED
    );

    // Hoặc cách đơn giản hơn nếu chỉ cần biết user có từng đặt hàng sản phẩm đó (không cần biết trạng thái)
    // @Query("SELECT COUNT(o.id) > 0 FROM Order o JOIN o.orderItems oi WHERE o.user.id = :userId AND oi.product.id = :productId")
    // boolean existsByUserIdAndItemsProductId(@Param("userId") Long userId, @Param("productId") Long productId);

}

// *** Ghi chú quan trọng về Query trên: ***
// Cách implement hàm checkIfUserPurchasedProduct bằng query trực tiếp trên OrderRepository như trên
// có thể không phải là cách tối ưu nhất nếu user có RẤT NHIỀU đơn hàng.
// Một cách tiếp cận khác có thể là tạo một bảng riêng `user_purchased_products`
// được cập nhật khi đơn hàng chuyển sang trạng thái DELIVERED,
// và chỉ cần query trên bảng đó sẽ nhanh hơn nhiều.
// Tuy nhiên, với lượng đơn hàng vừa phải, query trên là đủ dùng.
// Chúng ta sẽ cần truyền OrderStatus.DELIVERED vào khi gọi hàm này.


