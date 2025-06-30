package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.Product;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.query.Param; 
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    /**
     * Tìm tất cả sản phẩm được sắp xếp theo số lượng bán giảm dần
     * @return List of products sorted by number sold
     */
    List<Product> findAllByOrderBySoldCountDesc();

    // Tìm tất cả sản phẩm thuộc về một Category cụ thể (có phân trang)
    // Sử dụng ID của Category để tìm kiếm
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Tìm tất cả sản phẩm thuộc về một Category cụ thể (dùng đối tượng Category)
    // Page<Product> findByCategory(Category category, Pageable pageable); // Cách khác

    // Tìm sản phẩm có tiêu đề chứa một chuỗi (không phân biệt chữ hoa/thường, có phân trang)
    Page<Product> findByTitleContainingIgnoreCase(String titleKeyword, Pageable pageable);

    // Tìm sản phẩm có tác giả chứa một chuỗi (không phân biệt chữ hoa/thường, có phân trang)
    Page<Product> findByAuthorContainingIgnoreCase(String authorKeyword, Pageable pageable);

    // Ví dụ về truy vấn phức tạp hơn sử dụng JPQL (Java Persistence Query Language)
    // Tìm sản phẩm theo keyword trong title hoặc author (có phân trang)
    @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE LOWER(concat('%', :keyword, '%')) OR LOWER(p.author) LIKE LOWER(concat('%', :keyword, '%'))")
    Page<Product> searchByTitleOrAuthor(@Param("keyword") String keyword, Pageable pageable);

    // Đếm số lượng sản phẩm theo category ID (dùng để kiểm tra trước khi xóa Category)
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    // Tìm sản phẩm theo nhiều danh mục
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id IN :categoryIds")
    Page<Product> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    // Tìm sản phẩm theo từ khóa trong tiêu đề hoặc tác giả
    @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

       
    /**
     * Tìm sản phẩm theo ID với các quan hệ category và categories được tải sớm
     * This prevents N+1 query issues by loading all related data in a single query
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.categories " +
           "WHERE p.productId = :productId")
    Optional<Product> findByProductIdWithCategoriesFetched(@Param("productId") UUID productId);

    /**
     * Tìm tất cả sản phẩm với các quan hệ category và categories được tải sớm
     * Để tránh vấn đề N+1 query khi tải danh sách sản phẩm
     */
    @Query(value = "SELECT DISTINCT p FROM Product p " +
                   "LEFT JOIN FETCH p.category " +
                   "LEFT JOIN FETCH p.categories",
           countQuery = "SELECT COUNT(p) FROM Product p")
    Page<Product> findAllWithCategoriesFetched(Pageable pageable);

    // *** AI CHATBOT OPTIMIZED QUERIES - PREVENT LAZY INITIALIZATION ***
    
    /**
     * Tìm sản phẩm theo tiêu đề hoặc tác giả với các quan hệ category được tải sớm
     * Để tránh LazyInitializationException khi truy cập thông tin category
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "WHERE LOWER(p.title) LIKE LOWER(concat('%', :keyword, '%')) OR LOWER(p.author) LIKE LOWER(concat('%', :keyword, '%'))")
    Page<Product> searchByTitleOrAuthorWithCategory(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Tìm sản phẩm theo tiêu đề chứa từ khóa với các quan hệ category được tải sớm
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "WHERE LOWER(p.title) LIKE LOWER(concat('%', :keyword, '%'))")
    Page<Product> findByTitleContainingIgnoreCaseWithCategory(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Tìm sản phẩm theo tác giả chứa từ khóa với các quan hệ category được tải sớm
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "WHERE LOWER(p.author) LIKE LOWER(concat('%', :keyword, '%'))")
    Page<Product> findByAuthorContainingIgnoreCaseWithCategory(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Tìm tất cả sản phẩm được sắp xếp theo số lượng bán giảm dần với các quan hệ category được tải sớm
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "ORDER BY p.soldCount DESC")
    List<Product> findAllByOrderBySoldCountDescWithCategory();

}