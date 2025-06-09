package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.review.CreateReviewRequest;
import com.michaelcao.bookstore_backend.dto.review.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {

    /**
     * Thêm một đánh giá mới cho một sản phẩm.
     * Yêu cầu người dùng phải đăng nhập và (tùy chọn) đã mua sản phẩm này.
     * @param userId ID của người dùng viết đánh giá.
     * @param productId ID của sản phẩm được đánh giá.
     * @param request DTO chứa thông tin rating và comment.
     * @return ReviewDTO của đánh giá vừa tạo.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu productId hoặc userId không tồn tại.
     * @throws com.michaelcao.bookstore_backend.exception.OperationNotAllowedException Nếu người dùng chưa mua sản phẩm hoặc đã đánh giá sản phẩm này rồi.
     * @throws com.michaelcao.bookstore_backend.exception.DuplicateResourceException Nếu người dùng đã đánh giá sản phẩm này.
     */
    ReviewDTO addReview(Long userId, UUID productId, CreateReviewRequest request);

    /**
     * Lấy danh sách các đánh giá cho một sản phẩm cụ thể (có phân trang).
     * @param productId ID của sản phẩm.
     * @param pageable Thông tin phân trang và sắp xếp (ví dụ: sắp xếp theo ngày mới nhất).
     * @return Page chứa danh sách ReviewDTO.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu productId không tồn tại.
     */
    Page<ReviewDTO> getReviewsByProductId(UUID productId, Pageable pageable);

    /**
     * Xóa một đánh giá (chỉ cho phép người dùng xóa đánh giá của chính họ).
     * @param reviewId ID của đánh giá cần xóa.
     * @param userId ID của người dùng yêu cầu xóa (để kiểm tra quyền).
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu reviewId không tồn tại.
     * @throws com.michaelcao.bookstore_backend.exception.OperationNotAllowedException Nếu người dùng không có quyền xóa đánh giá này.
     */
    void deleteReview(Long reviewId, Long userId);

    /**
     * Cập nhật một đánh giá (chỉ cho phép người dùng cập nhật đánh giá của chính họ).
     * @param reviewId ID của đánh giá cần cập nhật.
     * @param userId ID của người dùng yêu cầu cập nhật.
     * @param request DTO chứa thông tin cập nhật.
     * @return ReviewDTO sau khi cập nhật.
     */
    ReviewDTO updateReview(Long reviewId, Long userId, CreateReviewRequest request);
}