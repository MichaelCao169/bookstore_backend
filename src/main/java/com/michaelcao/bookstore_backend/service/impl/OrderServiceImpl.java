package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.dto.order.CreateOrderRequest;
import com.michaelcao.bookstore_backend.dto.order.OrderDTO;
import com.michaelcao.bookstore_backend.dto.order.OrderItemDTO;
import com.michaelcao.bookstore_backend.dto.order.UpdateOrderStatusRequest;
import com.michaelcao.bookstore_backend.entity.*; // Import các entity cần thiết (Order, OrderItem, User, Cart, CartItem, Product, Address, OrderStatus, PaymentMethod)
import com.michaelcao.bookstore_backend.exception.OperationNotAllowedException;
import com.michaelcao.bookstore_backend.exception.ResourceNotFoundException;
import com.michaelcao.bookstore_backend.repository.*; // Import các repository (Order, OrderItem, User, Cart, CartItem, Product)
import com.michaelcao.bookstore_backend.service.CartService; // Import CartService để xóa giỏ hàng
import com.michaelcao.bookstore_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // QUAN TRỌNG cho createOrder
import com.michaelcao.bookstore_backend.entity.OrderStatus; // Import Enum
import java.math.BigDecimal;
import java.util.HashSet; // Import HashSet
import java.util.Set;     // Import Set
import java.util.stream.Collectors;
import java.util.Collections; // Import Collections
import java.util.List;        // Import List
import java.util.Map;         // Import Map

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    // --- Helper method: Map Order entity sang OrderDTO ---
// Đảm bảo hàm này xử lý trường hợp user/items có thể được fetch sẵn
    private OrderDTO mapToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getId());
        // User chắc chắn đã được fetch bởi các query mới
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUserName(order.getUser().getName());
            dto.setUserEmail(order.getUser().getEmail());
        } else {
            log.warn("User is null for Order ID: {}", order.getId()); // Should not happen with FETCH
        }
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setNotes(order.getNotes());

        // Map Address
        if (order.getShippingAddress() != null) {
            dto.setShippingStreet(order.getShippingAddress().getStreet());
            dto.setShippingCity(order.getShippingAddress().getCity());
            dto.setShippingDistrict(order.getShippingAddress().getDistrict());
            dto.setShippingCountry(order.getShippingAddress().getCountry());
            dto.setShippingPhone(order.getShippingAddress().getPhone());
            dto.setShippingRecipientName(order.getShippingAddress().getRecipientName());
        }

        // Map OrderItems (Items và Product cũng đã được fetch trong findByIdWithDetails)
        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(this::mapToOrderItemDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    // --- Helper method: Map OrderItem entity sang OrderItemDTO ---
    // Đảm bảo hàm này xử lý product đã được fetch sẵn
    private OrderItemDTO mapToOrderItemDTO(OrderItem item) {
        // Product đã được fetch cùng OrderItem trong findByIdWithDetails
        if (item.getProduct() == null) {
            log.warn("OrderItem ID {} references a missing product!", item.getId());
            return new OrderItemDTO(item.getId(), null, "[Product Deleted]", null, null, item.getQuantity(), item.getPriceAtPurchase());
        }
        return new OrderItemDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getTitle(),
                item.getProduct().getAuthor(),
                item.getProduct().getImageUrl(),
                item.getQuantity(),
                item.getPriceAtPurchase()
        );
    }

    @Override
    @Transactional // *** Rất quan trọng: Đảm bảo tất cả thao tác (check, save, update, delete) thành công hoặc rollback ***
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        log.info("Attempting to create order for user ID: {}", userId);

        // 1. Lấy thông tin User và Cart (dùng JOIN FETCH để lấy cả CartItems và Product)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        Cart cart = cartRepository.findByUser_IdWithItemsAndProducts(userId)
                .orElseThrow(() -> new OperationNotAllowedException("Cannot create order: Cart not found or empty.")); // Hoặc trả về giỏ hàng rỗng nếu muốn

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new OperationNotAllowedException("Cannot create order: Cart is empty.");
        }

        // 2. Tạo đối tượng Order mới
        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNotes(request.getNotes());

        // 3. Tạo đối tượng Address từ request và set vào Order
        CreateOrderRequest.AddressInfo reqAddress = request.getShippingAddress();
        Address shippingAddress = new Address(
                reqAddress.getStreet(),
                reqAddress.getCity(),
                reqAddress.getDistrict(),
                reqAddress.getCountry(),
                reqAddress.getPhone(),
                // Lấy tên người nhận từ request, nếu không có thì dùng tên user
                reqAddress.getRecipientName() != null ? reqAddress.getRecipientName() : user.getName()
        );
        order.setShippingAddress(shippingAddress);

        // 4. Xử lý OrderItems và kiểm tra tồn kho LẦN CUỐI
        Set<OrderItem> orderItems = new HashSet<>();
        BigDecimal calculatedTotalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            int requestedQuantity = cartItem.getQuantity();

            // Kiểm tra lại product tồn tại (phòng trường hợp bị xóa sau khi thêm vào giỏ)
            if (product == null) {
                throw new ResourceNotFoundException("Product with ID " + cartItem.getProduct().getId() + " in cart not found.");
            }

            // Kiểm tra lại tồn kho
            if (product.getStockQuantity() < requestedQuantity) {
                log.warn("Order creation failed: Product ID {} ('{}') stock is insufficient ({} < {}).",
                        product.getId(), product.getTitle(), product.getStockQuantity(), requestedQuantity);
                throw new OperationNotAllowedException("Insufficient stock for product: " + product.getTitle() +
                        ". Available: " + product.getStockQuantity() +
                        ", Requested: " + requestedQuantity);
            }

            // Tạo OrderItem mới
            OrderItem orderItem = new OrderItem(
                    order, // Tham chiếu đến Order đang tạo
                    product,
                    requestedQuantity,
                    product.getPrice() // Lấy giá hiện tại của sản phẩm làm giá tại thời điểm mua
            );
            orderItems.add(orderItem);

            // Trừ kho sản phẩm
            product.setStockQuantity(product.getStockQuantity() - requestedQuantity);
            // KHÔNG cần gọi productRepository.save() ở đây vì đang trong transaction,
            // Hibernate sẽ tự động phát hiện thay đổi và cập nhật khi transaction commit.

            // Tính tổng tiền
            calculatedTotalAmount = calculatedTotalAmount.add(
                    product.getPrice().multiply(BigDecimal.valueOf(requestedQuantity))
            );
        }

        // 5. Set tổng tiền và OrderItems vào Order
        order.setTotalAmount(calculatedTotalAmount);
        order.setOrderItems(orderItems); // Set này sẽ tự động cascade lưu OrderItems khi lưu Order

        // 6. Set trạng thái ban đầu dựa trên phương thức thanh toán
        if (request.getPaymentMethod() == PaymentMethod.COD) {
            order.setStatus(OrderStatus.PENDING); // COD thì chờ xử lý
        } else {
            // Các phương thức thanh toán online khác (VNPAY,...)
            order.setStatus(OrderStatus.PENDING_PAYMENT); // Chờ thanh toán
            // *** Logic gọi VNPayService.createPaymentUrl() sẽ nằm ở đây nếu là VNPAY ***
            // Hiện tại chỉ lưu đơn hàng chờ thanh toán.
        }

        // 7. Lưu Order (sẽ cascade lưu OrderItems và cập nhật Product stock)
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // 8. Nếu là COD (hoặc thanh toán thành công ngay), xóa giỏ hàng
        if (savedOrder.getStatus() != OrderStatus.PENDING_PAYMENT && savedOrder.getStatus() != OrderStatus.PAYMENT_FAILED) {
            // Chỉ xóa giỏ hàng khi đơn hàng không còn ở trạng thái chờ thanh toán
            cartService.clearCart(userId);
            log.info("Cart cleared for user ID: {}", userId);
        }


        // 9. Map sang DTO để trả về - *** KHÔNG CẦN QUERY LẠI ***
        // Map trực tiếp từ savedOrder (đã có user và items được gán)
        // Cần đảm bảo Product trong items cũng được load nếu mapToOrderItemDTO cần
        // (Trong trường hợp này, Product được lấy từ CartItem đã fetch sẵn nên OK)
        return mapToOrderDTO(savedOrder); // Sử dụng savedOrder trực tiếp
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching orders for user ID: {} with pagination: {}", userId, pageable);

        // 1. Query 1: Lấy Page<Order> với User đã fetch
        Page<Order> orderPage = orderRepository.findByUserIdWithUserOrderByOrderDateDesc(userId, pageable);
        List<Order> ordersOnPage = orderPage.getContent();

        // 2. Nếu có orders trên trang này, thực hiện Query 2
        if (!ordersOnPage.isEmpty()) {
            // Lấy danh sách các Order ID
            List<Long> orderIds = ordersOnPage.stream().map(Order::getId).collect(Collectors.toList());

            // Query 2: Lấy tất cả OrderItems và Products liên quan cho các Order ID đó
            List<OrderItem> allItemsForPage = orderItemRepository.findByOrderIdInWithProduct(orderIds);

            // 3. Gom nhóm các OrderItem theo Order ID để dễ dàng gắn vào Order
            Map<Long, List<OrderItem>> itemsByOrderIdMap = allItemsForPage.stream()
                    .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

            // 4. Gắn các OrderItem vào đối tượng Order tương ứng
            // Lưu ý: Collection orderItems trong Order entity có thể là unmodifiable proxy
            // nếu không được fetch. Chúng ta cần cách để set lại hoặc dùng DTO.
            // Cách an toàn hơn là không sửa đổi trực tiếp collection của Entity mà
            // tạo DTO và set list items vào DTO.
            // Tuy nhiên, chúng ta sẽ thử map trực tiếp ở đây và xem Hibernate xử lý thế nào.
            // Nếu không được thì sẽ map trong lúc tạo DTO.
            ordersOnPage.forEach(order ->
                    order.setOrderItems(new HashSet<>(itemsByOrderIdMap.getOrDefault(order.getId(), Collections.emptyList())))
            );
            // Dòng trên tạo một HashSet mới từ list lấy được và set lại vào Order.
            // Điều này hoạt động vì chúng ta đang trong transaction readOnly,
            // và việc set này chỉ ảnh hưởng đến đối tượng trong bộ nhớ, không ghi vào DB.
        }

        // 5. Map Page<Order> (giờ đã có đủ thông tin) sang Page<OrderDTO>
        return orderPage.map(this::mapToOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderDetails(Long userId, Long orderId) {
        // Giữ nguyên implementation này vì findByIdWithDetails đã tối ưu
        log.debug("Fetching order details for order ID: {} and user ID: {}", orderId, userId);
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "ID", orderId));

        if (!order.getUser().getId().equals(userId)) {
            log.warn("User ID {} attempted to access order ID {} which belongs to user ID {}", userId, orderId, order.getUser().getId());
            throw new ResourceNotFoundException("Order", "ID", orderId);
        }
        return mapToOrderDTO(order);
    }


    // --- Admin Methods ---
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        log.debug("Admin request: Fetching all orders with pagination: {}", pageable);

        // 1. Query 1: Lấy Page<Order> với User đã fetch
        Page<Order> orderPage = orderRepository.findAllWithUser(pageable);
        List<Order> ordersOnPage = orderPage.getContent();

        // 2. Nếu có orders, thực hiện Query 2 để lấy Items + Products
        if (!ordersOnPage.isEmpty()) {
            List<Long> orderIds = ordersOnPage.stream().map(Order::getId).collect(Collectors.toList());
            List<OrderItem> allItemsForPage = orderItemRepository.findByOrderIdInWithProduct(orderIds);
            Map<Long, List<OrderItem>> itemsByOrderIdMap = allItemsForPage.stream()
                    .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

            // 3. Gắn items vào orders
            ordersOnPage.forEach(order ->
                    order.setOrderItems(new HashSet<>(itemsByOrderIdMap.getOrDefault(order.getId(), Collections.emptyList())))
            );
        }

        // 4. Map sang DTO
        return orderPage.map(this::mapToOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderByIdForAdmin(Long orderId) {
        // Giữ nguyên implementation này vì findByIdWithDetails đã tối ưu
        log.debug("Admin request: Fetching order details for order ID: {}", orderId);
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> {
                    log.warn("Admin request failed: Order not found with ID: {}", orderId);
                    return new ResourceNotFoundException("Order", "ID", orderId);
                });
        return mapToOrderDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        log.info("Admin request: Updating status for order ID: {} to {}", orderId, request.getStatus());
        Order order = orderRepository.findById(orderId) // Chỉ cần tìm order theo ID
                .orElseThrow(() -> {
                    log.warn("Admin status update failed: Order not found with ID: {}", orderId);
                    return new ResourceNotFoundException("Order", "ID", orderId);
                });

        OrderStatus newStatus = request.getStatus();
        OrderStatus currentStatus = order.getStatus();

        // Optional: Thêm logic kiểm tra việc chuyển đổi trạng thái có hợp lệ không
        // Ví dụ: không thể chuyển từ DELIVERED về PENDING
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            log.warn("Invalid status transition requested for order ID {}: from {} to {}", orderId, currentStatus, newStatus);
            throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order); // Lưu trạng thái mới
        log.info("Order status updated successfully for order ID: {}", orderId);

        // *** KHÔNG CẦN QUERY LẠI - Map từ updatedOrder ***
        // Tuy nhiên, updatedOrder có thể không có User/Items nếu query gốc findById không fetch.
        // Để đảm bảo DTO trả về đầy đủ sau khi update, có 2 cách:
        // Cách 1: Fetch lại (như code cũ - tốn thêm query)
        Order fetchedOrder = orderRepository.findByIdWithDetails(updatedOrder.getId()).orElse(updatedOrder);
        return mapToOrderDTO(fetchedOrder);

        // Cách 2: Map từ updatedOrder, chấp nhận User/Items có thể là proxy/null nếu DTO không cần
        // Cần đảm bảo `mapToOrderDTO` xử lý null an toàn.
        // return mapToOrderDTO(updatedOrder);
    }




    // Optional: Helper method để kiểm tra logic chuyển đổi trạng thái
    private boolean isValidStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == next) return true; // Luôn cho phép giữ nguyên
        // Ví dụ: không cho quay lại trạng thái cũ hơn PENDING
        if (next == OrderStatus.PENDING && current != OrderStatus.PENDING_PAYMENT) return false;
        // Ví dụ: không cho chuyển từ trạng thái cuối (DELIVERED, CANCELLED)
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) return false;
        // Thêm các quy tắc khác nếu cần
        return true;
    }

}