package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.Review;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import Query
import org.springframework.data.repository.query.Param; // Import Param
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> { // Entity: Review, ID: Long

    /**
     * Tìm tất cả các đánh giá cho một Product ID cụ thể (có phân trang).
     * Đồng thời tải luôn thông tin User để hiển thị tên người đánh giá.
     * @param productId ID của Product.
     * @param pageable Thông tin phân trang.
     * @return Page chứa danh sách Review với User đã được tải.
     */
    @Query(value = "SELECT r FROM Review r LEFT JOIN FETCH r.user u WHERE r.product.id = :productId",
            countQuery = "SELECT count(r) FROM Review r WHERE r.product.id = :productId")
    Page<Review> findByProductIdWithUser(@Param("productId") Long productId, Pageable pageable);

    /**
     * Kiểm tra xem một User đã đánh giá một Product cụ thể chưa.
     * @param userId ID của User.
     * @param productId ID của Product.
     * @return true nếu đã tồn tại đánh giá, false nếu chưa.
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * Tìm đánh giá cụ thể của một User cho một Product.
     * @param userId ID của User.
     * @param productId ID của Product.
     * @return Optional chứa Review nếu tìm thấy.
     */
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * Tính rating trung bình cho một sản phẩm.
     * Trả về null nếu sản phẩm chưa có đánh giá nào.
     * @param productId ID của Product.
     * @return Giá trị trung bình kiểu Double, hoặc null.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * Đếm số lượng đánh giá cho một sản phẩm.
     * @param productId ID của Product.
     * @return Số lượng đánh giá kiểu Long.
     */
    // Spring Data có thể tự tạo query này từ tên, hoặc dùng @Query cho chắc chắn
    // long countByProductId(Long productId);
    // Hoặc:
    @Query("SELECT COUNT(r.id) FROM Review r WHERE r.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);


    // *** QUERY TỐI ƯU CHO N+1 ***
    /**
     * Lấy rating trung bình và số lượng review cho một danh sách các Product ID.
     * Trả về một List các Object[], mỗi Object[] chứa [productId, averageRating, reviewCount].
     * @param productIds Danh sách các ID của Product.
     * @return List chứa kết quả tổng hợp.
     */
    @Query("SELECT r.product.id as productId, AVG(r.rating) as averageRating, COUNT(r.id) as reviewCount " +
            "FROM Review r WHERE r.product.id IN :productIds " +
            "GROUP BY r.product.id")
    List<Object[]> findReviewStatsByProductIds(@Param("productIds") List<Long> productIds);

    // --- Interface để map kết quả từ query findReviewStatsByProductIds (Cách khác thay vì Object[]) ---
    interface ReviewStats {
        Long getProductId();
        Double getAverageRating();
        Long getReviewCount();
    }

    @Query("SELECT r.product.id as productId, AVG(r.rating) as averageRating, COUNT(r.id) as reviewCount " +
            "FROM Review r WHERE r.product.id IN :productIds " +
            "GROUP BY r.product.id")
    List<ReviewStats> findReviewStatsByProductIdsProjection(@Param("productIds") List<Long> productIds);

}