package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.cart.AddToCartRequest;
import com.michaelcao.bookstore_backend.dto.cart.CartDTO;
import com.michaelcao.bookstore_backend.dto.cart.UpdateCartItemRequest;

public interface CartService {

    /**
     * Lấy thông tin chi tiết giỏ hàng của người dùng đang đăng nhập.
     * Nếu người dùng chưa có giỏ hàng, một giỏ hàng mới sẽ được tạo.
     * @param userId ID của người dùng đang đăng nhập.
     * @return CartDTO chứa thông tin giỏ hàng.
     */
    CartDTO getCartByUserId(Long userId);

    /**
     * Thêm một sản phẩm vào giỏ hàng của người dùng đang đăng nhập.
     * Nếu sản phẩm đã tồn tại trong giỏ, số lượng sẽ được cộng dồn.
     * Kiểm tra số lượng tồn kho trước khi thêm.
     * @param userId ID của người dùng đang đăng nhập.
     * @param request DTO chứa productId và quantity.
     * @return CartDTO giỏ hàng sau khi đã thêm sản phẩm.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu productId không tồn tại.
     * @throws com.michaelcao.bookstore_backend.exception.OperationNotAllowedException Nếu số lượng yêu cầu vượt quá tồn kho.
     */
    CartDTO addProductToCart(Long userId, AddToCartRequest request);

    /**
     * Cập nhật số lượng của một món hàng trong giỏ.
     * Kiểm tra số lượng tồn kho trước khi cập nhật.
     * @param userId ID của người dùng đang đăng nhập (để đảm bảo họ sở hữu giỏ hàng).
     * @param cartItemId ID của CartItem cần cập nhật.
     * @param request DTO chứa quantity mới.
     * @return CartDTO giỏ hàng sau khi cập nhật.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu cartItemId không tồn tại hoặc không thuộc về người dùng.
     * @throws com.michaelcao.bookstore_backend.exception.OperationNotAllowedException Nếu số lượng yêu cầu vượt quá tồn kho.
     */
    CartDTO updateCartItemQuantity(Long userId, Long cartItemId, UpdateCartItemRequest request);

    /**
     * Xóa một món hàng khỏi giỏ hàng.
     * @param userId ID của người dùng đang đăng nhập.
     * @param cartItemId ID của CartItem cần xóa.
     * @return CartDTO giỏ hàng sau khi xóa.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu cartItemId không tồn tại hoặc không thuộc về người dùng.
     */
    CartDTO removeCartItem(Long userId, Long cartItemId);

    /**
     * Xóa toàn bộ nội dung giỏ hàng của người dùng (ví dụ: sau khi đặt hàng thành công).
     * @param userId ID của người dùng đang đăng nhập.
     */
    void clearCart(Long userId); // Thêm phương thức này
}