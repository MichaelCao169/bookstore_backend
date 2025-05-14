package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.dto.cart.AddToCartRequest;
import com.michaelcao.bookstore_backend.dto.cart.CartDTO;
import com.michaelcao.bookstore_backend.dto.cart.CartItemDTO;
import com.michaelcao.bookstore_backend.dto.cart.UpdateCartItemRequest;
import com.michaelcao.bookstore_backend.entity.Cart;
import com.michaelcao.bookstore_backend.entity.CartItem;
import com.michaelcao.bookstore_backend.entity.Product;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.exception.OperationNotAllowedException;
import com.michaelcao.bookstore_backend.exception.ResourceNotFoundException;
import com.michaelcao.bookstore_backend.repository.CartItemRepository;
import com.michaelcao.bookstore_backend.repository.CartRepository;
import com.michaelcao.bookstore_backend.repository.ProductRepository;
import com.michaelcao.bookstore_backend.repository.UserRepository;
import com.michaelcao.bookstore_backend.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, 
                         CartItemRepository cartItemRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // --- Helper Method: Lấy hoặc tạo Cart cho User ---
    private Cart getOrCreateCart(Long userId) {
        // Sử dụng query JOIN FETCH để tối ưu việc tải items và products
        return cartRepository.findByUser_IdWithItemsAndProducts(userId)
                .orElseGet(() -> {
                    log.info("No cart found for user ID: {}. Creating a new cart.", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });
    }

    // --- Helper Method: Map Cart Entity sang CartDTO và tính toán ---
    private CartDTO mapCartToCartDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO(cart.getId());
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalItemsCount = 0;

        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                if (item.getProduct() != null) { // Kiểm tra product có tồn tại không
                    CartItemDTO itemDTO = new CartItemDTO(
                            item.getId(),
                            item.getQuantity(),
                            item.getProduct().getId(),
                            item.getProduct().getTitle(),
                            item.getProduct().getAuthor(),
                            item.getProduct().getPrice(),
                            item.getProduct().getImageUrl(),
                            item.getProduct().getStockQuantity()
                    );
                    // Tính subtotal cho item này
                    BigDecimal itemSubtotal = item.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    itemDTO.setSubtotal(itemSubtotal);
                    cartDTO.getItems().add(itemDTO);

                    // Cộng dồn vào tổng giá và tổng số lượng
                    totalPrice = totalPrice.add(itemSubtotal);
                    totalItemsCount += item.getQuantity();
                } else {
                    log.warn("CartItem ID {} references a missing product. Skipping item.", item.getId());
                    // Có thể xóa cart item này nếu product không còn tồn tại
                    // cartItemRepository.delete(item); // Cẩn thận khi xóa tự động
                }
            }
        }

        cartDTO.setTotalPrice(totalPrice);
        cartDTO.setTotalItems(totalItemsCount);
        return cartDTO;
    }

    @Override
    @Transactional // Chỉ đọc, nhưng getOrCreateCart có thể ghi nếu tạo mới
    public CartDTO getCartByUserId(Long userId) {
        log.debug("Fetching cart for user ID: {}", userId);
        Cart cart = getOrCreateCart(userId);
        return mapCartToCartDTO(cart);
    }

    @Override
    @Transactional // Thao tác ghi vào DB
    public CartDTO addProductToCart(Long userId, AddToCartRequest request) {
        log.debug("Adding product ID {} with quantity {} to cart for user ID {}",
                request.getProductId(), request.getQuantity(), userId);

        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", request.getProductId()));

        // Kiểm tra số lượng tồn kho
        if (product.getStockQuantity() < request.getQuantity()) {
            log.warn("Cannot add product ID {}: Requested quantity {} exceeds stock {}",
                    product.getId(), request.getQuantity(), product.getStockQuantity());
            throw new OperationNotAllowedException("Requested quantity exceeds available stock for product: " + product.getTitle());
        }

        // Tìm xem sản phẩm đã có trong giỏ chưa
        Optional<CartItem> existingItemOptional = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst();

        if (existingItemOptional.isPresent()) {
            // Nếu đã có -> cộng dồn số lượng
            CartItem existingItem = existingItemOptional.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            // Kiểm tra lại tồn kho với số lượng mới
            if (product.getStockQuantity() < newQuantity) {
                log.warn("Cannot add product ID {}: New total quantity {} exceeds stock {}",
                        product.getId(), newQuantity, product.getStockQuantity());
                throw new OperationNotAllowedException("Adding requested quantity exceeds available stock for product: " + product.getTitle());
            }
            existingItem.setQuantity(newQuantity);
            log.info("Updated quantity for product ID {} in cart ID {}", product.getId(), cart.getId());
            // Không cần gọi save CartItem vì cascade=ALL và orphanRemoval=true trên Cart
        } else {
            // Nếu chưa có -> tạo CartItem mới
            CartItem newItem = new CartItem(cart, product, request.getQuantity());
            cart.addCartItem(newItem); // Thêm vào Set và tự động set quan hệ hai chiều
            log.info("Added new product ID {} to cart ID {}", product.getId(), cart.getId());
            // Không cần gọi save CartItem vì cascade=ALL
        }

        // Lưu lại Cart (sẽ tự động cascade đến CartItem nhờ CascadeType.ALL)
        Cart updatedCart = cartRepository.save(cart);
        // Tải lại với JOIN FETCH để lấy DTO chính xác
        return getCartByUserId(userId); // Gọi lại hàm này để đảm bảo DTO được tính toán đúng
    }

    @Override
    @Transactional
    public CartDTO updateCartItemQuantity(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        log.debug("Updating quantity for cart item ID {} to {} for user ID {}",
                cartItemId, request.getQuantity(), userId);

        // Lấy giỏ hàng để đảm bảo user sở hữu item này (hoặc kiểm tra trực tiếp cartItem.getCart().getUser().getId())
        Cart cart = getOrCreateCart(userId); // Lấy cart của user

        // Tìm CartItem theo ID VÀ đảm bảo nó thuộc về giỏ hàng của user
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId + " in your cart."));

        Product product = cartItem.getProduct();
        if (product == null) { // Nên kiểm tra
            log.error("Product not found for CartItem ID {}", cartItemId);
            // Xử lý lỗi này, ví dụ xóa item lỗi
            cart.removeCartItem(cartItem);
            cartRepository.save(cart);
            throw new ResourceNotFoundException("Product associated with this cart item no longer exists.");
        }

        // Kiểm tra số lượng tồn kho
        if (product.getStockQuantity() < request.getQuantity()) {
            log.warn("Cannot update cart item ID {}: Requested quantity {} exceeds stock {}",
                    cartItemId, request.getQuantity(), product.getStockQuantity());
            throw new OperationNotAllowedException("Requested quantity exceeds available stock for product: " + product.getTitle());
        }

        // Cập nhật số lượng
        cartItem.setQuantity(request.getQuantity());
        log.info("Updated quantity for cart item ID {} successfully.", cartItemId);

        // Lưu lại Cart (cascade sẽ cập nhật CartItem)
        cartRepository.save(cart);
        // Tải lại với JOIN FETCH để lấy DTO chính xác
        return getCartByUserId(userId);
    }

    @Override
    @Transactional
    public CartDTO removeCartItem(Long userId, Long cartItemId) {
        log.debug("Removing cart item ID {} for user ID {}", cartItemId, userId);

        Cart cart = getOrCreateCart(userId);

        // Tìm CartItem trong Set của Cart
        Optional<CartItem> itemToRemoveOptional = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst();

        if (itemToRemoveOptional.isPresent()) {
            CartItem itemToRemove = itemToRemoveOptional.get();
            // Sử dụng helper method hoặc remove trực tiếp từ Set
            // cart.removeCartItem(itemToRemove); // Dùng helper nếu có
            cart.getCartItems().remove(itemToRemove); // Remove khỏi collection
            // Do orphanRemoval=true, khi save(cart), item này sẽ bị xóa khỏi DB
            cartRepository.save(cart);
            log.info("Removed cart item ID {} successfully.", cartItemId);
        } else {
            log.warn("Cart item ID {} not found in cart for user ID {}. No item removed.", cartItemId, userId);
            // Không ném lỗi vì client có thể gửi ID sai, chỉ cần không làm gì cả
            // Hoặc có thể ném ResourceNotFoundException nếu muốn báo lỗi rõ ràng
            // throw new ResourceNotFoundException("Cart item not found with ID: " + cartItemId + " in your cart.");
        }

        // Trả về giỏ hàng sau khi xóa (hoặc không đổi nếu không tìm thấy item)
        return getCartByUserId(userId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        log.debug("Clearing cart for user ID: {}", userId);
        Cart cart = getOrCreateCart(userId);

        if (!cart.getCartItems().isEmpty()) {
            // Cách 1: Dùng orphanRemoval (nếu tin tưởng cascade)
            cart.getCartItems().clear(); // Xóa tất cả item khỏi Set
            cartRepository.save(cart); // Orphan removal sẽ xóa các CartItem khỏi DB

            // Cách 2: Xóa trực tiếp bằng Repository (an toàn hơn nếu không chắc về cascade)
            // cartItemRepository.deleteByCartId(cart.getId());
            log.info("Cart cleared successfully for user ID: {}", userId);
        } else {
            log.debug("Cart for user ID {} was already empty.", userId);
        }
    }
}