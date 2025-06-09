package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.dto.review.CreateReviewRequest;
import com.michaelcao.bookstore_backend.dto.review.ReviewDTO;
import com.michaelcao.bookstore_backend.entity.*;
import com.michaelcao.bookstore_backend.exception.DuplicateResourceException;
import com.michaelcao.bookstore_backend.exception.OperationNotAllowedException;
import com.michaelcao.bookstore_backend.exception.ResourceNotFoundException;
import com.michaelcao.bookstore_backend.repository.OrderRepository; // Import OrderRepository
import com.michaelcao.bookstore_backend.repository.ProductRepository;
import com.michaelcao.bookstore_backend.repository.ReviewRepository;
import com.michaelcao.bookstore_backend.repository.UserRepository;
import com.michaelcao.bookstore_backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository; // Inject để kiểm tra lịch sử mua hàng

    // --- Helper method: Map Review entity sang ReviewDTO ---
    private ReviewDTO mapToReviewDTO(Review review) {
        // User đã được fetch cùng Review nhờ query findByProductIdWithUser
        String userName = (review.getUser() != null) ? review.getUser().getName() : "Unknown User";
        Long userId = (review.getUser() != null) ? review.getUser().getId() : null;

        return new ReviewDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                userId,
                userName
        );
    }

    // --- Helper method: Kiểm tra xem user đã mua sản phẩm chưa ---
    // Trong ReviewServiceImpl.java
    private boolean checkIfUserPurchasedProduct(Long userId, UUID productId) {
        // Gọi đúng phương thức repository và truyền trạng thái DELIVERED
        boolean hasPurchased = orderRepository.existsByUserIdAndItemsProductIdAndStatusDelivered(userId, productId, OrderStatus.DELIVERED);
        log.debug("Check purchase status: User ID {} purchased and received product ID {}: {}", userId, productId, hasPurchased);
        return hasPurchased;
    }

    @Override
    @Transactional // Cần transaction vì có ghi dữ liệu
    public ReviewDTO addReview(Long userId, UUID productId, CreateReviewRequest request) {
        log.info("Attempting to add review for product ID {} by user ID {}", productId, userId);

        // 1. Lấy thông tin User và Product
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", productId));        // 2. Kiểm tra xem user đã đánh giá sản phẩm này chưa
        if (reviewRepository.existsByUserIdAndProductProductId(userId, productId)) {
            log.warn("User ID {} already reviewed product ID {}", userId, productId);
            throw new DuplicateResourceException("You have already reviewed this product.");
        }

        // 3. *** KIỂM TRA ĐIỀU KIỆN MUA HÀNG (QUAN TRỌNG) ***
        // Bỏ comment dòng này nếu bạn muốn bắt buộc user phải mua hàng trước khi đánh giá

        if (!checkIfUserPurchasedProduct(userId, productId)) {
            log.warn("User ID {} attempted to review product ID {} without purchasing and receiving it.", userId, productId);
            throw new OperationNotAllowedException("You can only review products you have purchased and received.");
        }
        // Nếu không cần kiểm tra mua hàng, hãy comment lại đoạn trên.

        // 4. Tạo đối tượng Review mới
        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setUser(user);
        review.setProduct(product);
        // createdAt sẽ được tự động tạo

        // 5. Lưu Review
        Review savedReview = reviewRepository.save(review);
        log.info("Review added successfully with ID: {}", savedReview.getId());

        // 6. Map và trả về DTO (Không cần fetch lại vì đã có đủ thông tin)
        return mapToReviewDTO(savedReview); // User đã có, Product không cần trong DTO này
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByProductId(UUID productId, Pageable pageable) {
        log.debug("Fetching reviews for product ID {} with pagination: {}", productId, pageable);
        // Kiểm tra Product tồn tại
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "ID", productId);
        }

        // Sử dụng query đã JOIN FETCH User
        Page<Review> reviewPage = reviewRepository.findByProductIdWithUser(productId, pageable);

        // Map sang DTO
        return reviewPage.map(this::mapToReviewDTO);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Attempting to delete review ID {} by user ID {}", reviewId, userId);

        // 1. Tìm review theo ID
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "ID", reviewId));

        // 2. Kiểm tra quyền - chỉ người tạo review mới có thể xóa
        if (!review.getUser().getId().equals(userId)) {
            log.warn("User ID {} attempted to delete review ID {} without permission", userId, reviewId);
            throw new OperationNotAllowedException("You can only delete your own reviews.");
        }

        // 3. Xóa review
        reviewRepository.delete(review);
        log.info("Review ID {} deleted successfully by user ID {}", reviewId, userId);
    }

    @Override
    @Transactional
    public ReviewDTO updateReview(Long reviewId, Long userId, CreateReviewRequest request) {
        log.info("Attempting to update review ID {} by user ID {}", reviewId, userId);

        // 1. Tìm review theo ID
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "ID", reviewId));

        // 2. Kiểm tra quyền - chỉ người tạo review mới có thể cập nhật
        if (!review.getUser().getId().equals(userId)) {
            log.warn("User ID {} attempted to update review ID {} without permission", userId, reviewId);
            throw new OperationNotAllowedException("You can only update your own reviews.");
        }

        // 3. Cập nhật thông tin
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        // 4. Lưu thay đổi
        Review updatedReview = reviewRepository.save(review);
        log.info("Review ID {} updated successfully by user ID {}", reviewId, userId);

        // 5. Map và trả về DTO
        return mapToReviewDTO(updatedReview);
    }
}