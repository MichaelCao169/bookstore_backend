package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.Order;
import com.michaelcao.bookstore_backend.entity.OrderStatus; 
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.query.Param; 
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional; 
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> { 

    /**
     * Tính tổng doanh thu từ các đơn hàng đã hoàn thành (không bao gồm đơn hàng bị hủy và đơn hàng thất bại)
     * @return BigDecimal total revenue
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status NOT IN ('CANCELLED', 'PAYMENT_FAILED')")
    BigDecimal getTotalRevenue();

    /**
     * Đếm tổng số đơn hàng hợp lệ (không bao gồm đơn hàng bị hủy và đơn hàng thất bại)
     * @return Long count of valid orders
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status NOT IN ('CANCELLED', 'PAYMENT_FAILED')")
    Long countValidOrders();

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
    Optional<Order> findByIdAndUserId(UUID orderId, Long userId);

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
    Optional<Order> findByIdWithDetails(@Param("orderId") UUID orderId);


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


   

    /**
     * Kiểm tra xem có tồn tại ít nhất một đơn hàng của user
     * đã được giao thành công (DELIVERED) và chứa sản phẩm với productId cho trước không.
     * @param userId ID của User.
     * @param productId ID của Product.
     * @return true nếu tồn tại đơn hàng thỏa mãn, false nếu không.
     */    @Query("SELECT COUNT(o.id) > 0 FROM Order o JOIN o.orderItems oi " +
            "WHERE o.user.id = :userId AND oi.product.productId = :productId AND o.status = :status")
    boolean existsByUserIdAndItemsProductIdAndStatusDelivered(
            @Param("userId") Long userId,
            @Param("productId") UUID productId,
            @Param("status") OrderStatus status // Truyền vào OrderStatus.DELIVERED
    );

   
}




