package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.Order; // Import Order
import com.michaelcao.bookstore_backend.entity.OrderItem;
import com.michaelcao.bookstore_backend.entity.Product; // Import Product
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> { // Entity: OrderItem, ID: UUID

    /**
     * Tìm tất cả OrderItem thuộc về một Order cụ thể.
     * (Thường không cần thiết vì có thể lấy qua order.getOrderItems(),
     * nhưng có thể hữu ích nếu cần truy vấn độc lập)
     * @param order Đối tượng Order.
     * @return List các OrderItem.
     */
    List<OrderItem> findByOrder(Order order);

    /**
     * Tìm tất cả OrderItem thuộc về một Order ID cụ thể.
     * @param orderId ID của Order.
     * @return List các OrderItem.
     */
    List<OrderItem> findByOrderId(UUID orderId);

    /**
     * Tìm một OrderItem cụ thể trong một Order dựa vào Product.
     * (Ít dùng hơn CartItem vì OrderItem thường không bị cập nhật sau khi tạo)
     * @param order Đối tượng Order.
     * @param product Đối tượng Product.
     * @return Optional chứa OrderItem nếu tìm thấy.
     */
    Optional<OrderItem> findByOrderAndProduct(Order order, Product product);    // Có thể thêm các query khác, ví dụ: đếm số lượng sản phẩm đã bán...
    // @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.productId = :productId")
    // Integer countTotalSoldByProductId(@Param("productId") UUID productId);
    /**
     * Tìm tất cả OrderItem thuộc về một danh sách các Order ID,
     * đồng thời tải luôn thông tin Product liên quan.
     * @param orderIds Danh sách các ID của Order.
     * @return List các OrderItem với Product đã được tải.
     */
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product p WHERE oi.order.id IN :orderIds")
    List<OrderItem> findByOrderIdInWithProduct(@Param("orderIds") List<UUID> orderIds);

    /**
     * Calculate total sold quantity for a product from delivered orders only
     * @param productId ID of the product
     * @return Total quantity sold from delivered orders
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi JOIN oi.order o WHERE oi.product.productId = :productId AND o.status = 'DELIVERED'")
    Integer calculateTotalSoldByProductId(@Param("productId") UUID productId);
}