package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.order.CreateOrderRequest;
import com.michaelcao.bookstore_backend.dto.order.OrderDTO;
import com.michaelcao.bookstore_backend.dto.order.UpdateOrderStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    /**
     * Tạo một đơn hàng mới cho người dùng dựa trên giỏ hàng hiện tại và thông tin yêu cầu.
     * Bao gồm việc kiểm tra tồn kho lần cuối, tính tổng tiền, tạo Order và OrderItems,
     * trừ kho sản phẩm, và xóa giỏ hàng (đối với COD).
     * @param userId ID của người dùng tạo đơn hàng.
     * @param request DTO chứa thông tin địa chỉ và phương thức thanh toán.
     * @return OrderDTO của đơn hàng vừa tạo.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu user hoặc sản phẩm trong giỏ không tồn tại.
     * @throws com.michaelcao.bookstore_backend.exception.OperationNotAllowedException Nếu giỏ hàng rỗng hoặc có sản phẩm hết hàng/không đủ số lượng.
     */
    OrderDTO createOrder(Long userId, CreateOrderRequest request);

    /**
     * Lấy danh sách đơn hàng của một người dùng cụ thể (có phân trang).
     * @param userId ID của người dùng.
     * @param pageable Thông tin phân trang.
     * @return Page chứa danh sách OrderDTO.
     */
    Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable);

    /**
     * Lấy thông tin chi tiết của một đơn hàng cụ thể của người dùng.
     * @param userId ID của người dùng.
     * @param orderId ID của đơn hàng.
     * @return OrderDTO chi tiết.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy đơn hàng hoặc đơn hàng không thuộc về người dùng.
     */
    OrderDTO getOrderDetails(Long userId, UUID orderId);

    /**
     * Hủy đơn hàng bởi khách hàng.
     * Chỉ cho phép hủy đơn hàng ở trạng thái PENDING hoặc PENDING_PAYMENT.
     * @param userId ID của người dùng thực hiện hủy đơn.
     * @param orderId ID của đơn hàng cần hủy.
     * @return OrderDTO của đơn hàng đã hủy.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu đơn hàng không tồn tại hoặc không thuộc về user.
     * @throws com.michaelcao.bookstore_backend.exception.OperationNotAllowedException Nếu đơn hàng không ở trạng thái cho phép hủy.
     */
    OrderDTO cancelOrder(Long userId, UUID orderId);

    // --- Admin Methods ---

    /**
     * Lấy danh sách tất cả đơn hàng (dành cho Admin, có phân trang).
     * @param pageable Thông tin phân trang.
     * @return Page chứa OrderDTO.
     */
    Page<OrderDTO> getAllOrders(Pageable pageable); // Bỏ comment hoặc thêm mới

    /**
     * Lấy chi tiết một đơn hàng bất kỳ (dành cho Admin).
     * @param orderId ID của đơn hàng.
     * @return OrderDTO chi tiết.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy đơn hàng.
     */
    OrderDTO getOrderByIdForAdmin(UUID orderId); // Bỏ comment hoặc thêm mới

    /**
     * Cập nhật trạng thái đơn hàng (dành cho Admin).
     * @param orderId ID của đơn hàng.
     * @param request DTO chứa trạng thái mới.
     * @return OrderDTO sau khi cập nhật.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy đơn hàng.
     * @throws IllegalArgumentException Nếu trạng thái không hợp lệ hoặc chuyển đổi trạng thái không hợp lệ.
     */
    OrderDTO updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request); // Sửa tham số thành DTO

}