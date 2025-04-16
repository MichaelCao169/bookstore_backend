package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.product.CreateProductRequest;
import com.michaelcao.bookstore_backend.dto.product.ProductDTO;
import com.michaelcao.bookstore_backend.dto.product.UpdateProductRequest;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import java.math.BigDecimal;
public interface ProductService {

    /**
     * Tạo một sản phẩm mới.
     * @param request DTO chứa thông tin sản phẩm mới.
     * @return ProductDTO của sản phẩm vừa tạo.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu categoryId không tồn tại.
     * @throws com.michaelcao.bookstore_backend.exception.DuplicateResourceException Nếu ISBN đã tồn tại.
     */
    ProductDTO createProduct(CreateProductRequest request);

    /**
     * Lấy thông tin chi tiết một sản phẩm theo ID.
     * @param id ID của sản phẩm.
     * @return ProductDTO của sản phẩm.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy sản phẩm.
     */
    ProductDTO getProductById(Long id);

    /**
     * Lấy danh sách tất cả sản phẩm (có phân trang).
     * @param pageable Thông tin phân trang và sắp xếp.
     * @return Page chứa danh sách ProductDTO.
     */
    Page<ProductDTO> getAllProducts(Pageable pageable);
    /**
     * Lấy danh sách sản phẩm có phân trang và hỗ trợ lọc nâng cao.
     * @param categoryId (Optional) Lọc theo ID danh mục.
     * @param keyword (Optional) Lọc theo từ khóa trong tên hoặc tác giả.
     * @param minPrice (Optional) Giá tối thiểu.
     * @param maxPrice (Optional) Giá tối đa.
     * @param inStockOnly (Optional) Chỉ lấy sản phẩm còn hàng (true) hay không (null hoặc false).
     * @param author (Optional) Lọc theo tên tác giả chính xác (không phân biệt hoa thường).
     * @param pageable Thông tin phân trang và sắp xếp.
     * @return Page chứa danh sách ProductDTO thỏa mãn điều kiện.
     */
    Page<ProductDTO> filterProducts( // Đổi tên thành filterProducts hoặc giữ getAllProducts và thêm tham số
                                     Long categoryId,
                                     String keyword,
                                     BigDecimal minPrice,
                                     BigDecimal maxPrice,
                                     Boolean inStockOnly,
                                     String author,
                                     Pageable pageable);
    /**
     * Lấy danh sách sản phẩm thuộc một danh mục cụ thể (có phân trang).
     * @param categoryId ID của danh mục.
     * @param pageable Thông tin phân trang và sắp xếp.
     * @return Page chứa danh sách ProductDTO.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu categoryId không tồn tại.
     */
    Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable);

    /**
     * Cập nhật thông tin một sản phẩm.
     * @param id ID của sản phẩm cần cập nhật.
     * @param request DTO chứa thông tin cập nhật.
     * @return ProductDTO của sản phẩm sau khi cập nhật.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu productId hoặc categoryId không tồn tại.
     * @throws com.michaelcao.bookstore_backend.exception.DuplicateResourceException Nếu ISBN mới trùng với ISBN khác (ngoại trừ chính nó).
     */
    ProductDTO updateProduct(Long id, UpdateProductRequest request);

    /**
     * Xóa một sản phẩm.
     * @param id ID của sản phẩm cần xóa.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy sản phẩm.
     */
    void deleteProduct(Long id);

    // Thêm các phương thức khác nếu cần (search, updateStock,...)
    // Page<ProductDTO> searchProducts(String keyword, Pageable pageable);
    /**
     * Tìm kiếm sản phẩm theo từ khóa trong tiêu đề hoặc tác giả (có phân trang).
     * @param keyword Từ khóa tìm kiếm.
     * @param pageable Thông tin phân trang và sắp xếp.
     * @return Page chứa danh sách ProductDTO tìm thấy.
     */
    Page<ProductDTO> searchProducts(String keyword, Pageable pageable);
}