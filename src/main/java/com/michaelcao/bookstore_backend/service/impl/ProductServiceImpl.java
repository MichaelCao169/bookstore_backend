package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.dto.category.CategoryDTO; // Import CategoryDTO
import com.michaelcao.bookstore_backend.dto.product.CreateProductRequest;
import com.michaelcao.bookstore_backend.dto.product.ProductDTO;
import com.michaelcao.bookstore_backend.dto.product.UpdateProductRequest;
import com.michaelcao.bookstore_backend.entity.Category;
import com.michaelcao.bookstore_backend.entity.OrderStatus;
import com.michaelcao.bookstore_backend.entity.Product;
import com.michaelcao.bookstore_backend.exception.DuplicateResourceException;
import com.michaelcao.bookstore_backend.exception.ResourceNotFoundException;
import com.michaelcao.bookstore_backend.repository.CategoryRepository;
import com.michaelcao.bookstore_backend.repository.OrderRepository;
import com.michaelcao.bookstore_backend.repository.ProductRepository;
import com.michaelcao.bookstore_backend.repository.specification.ProductSpecification;
import com.michaelcao.bookstore_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Import StringUtils
import com.michaelcao.bookstore_backend.repository.ReviewRepository;
import com.michaelcao.bookstore_backend.repository.ReviewRepository.ReviewStats;
import java.util.List; // Import List
import java.util.Map;  // Import Map
import java.util.function.Function; // Import Function
import java.util.stream.Collectors;
import java.util.Collections; // Import Collections
import java.math.BigDecimal;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    // --- Helper methods for mapping ---
    private ProductDTO mapToProductDTO(Product product, ReviewStats stats) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setAuthor(product.getAuthor());
        dto.setIsbn(product.getIsbn());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        dto.setPublishedDate(product.getPublishedDate());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // Map Category chính sang CategoryDTO
        if (product.getCategory() != null) {
            dto.setCategory(new CategoryDTO(
                    product.getCategory().getId(),
                    product.getCategory().getName(),
                    product.getCategory().getDescription()
            ));
        }
        
        // Map nhiều Category
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            dto.setCategories(product.getCategories().stream()
                    .map(category -> new CategoryDTO(
                            category.getId(),
                            category.getName(),
                            category.getDescription()
                    ))
                    .collect(Collectors.toList()));
        }
        
        // *** SET THÔNG TIN REVIEW TỪ STATS ***
        if (stats != null) {
            // Làm tròn rating đến 1 chữ số thập phân nếu cần
            dto.setAverageRating(stats.getAverageRating() != null ? Math.round(stats.getAverageRating() * 10.0) / 10.0 : 0.0);
            dto.setReviewCount(stats.getReviewCount() != null ? stats.getReviewCount() : 0L);
        } else {
            // Nếu không có stats (ví dụ sản phẩm chưa có review nào)
            dto.setAverageRating(0.0);
            dto.setReviewCount(0L);
        }
        return dto;
    }
    // Overload mapToProductDTO để dùng khi không có stats (ví dụ khi tạo mới)
    private ProductDTO mapToProductDTO(Product product) {
        return mapToProductDTO(product, null); // Gọi hàm map chính với stats là null
    }

    // Helper để tìm Category hoặc ném Exception
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", categoryId);
                    return new ResourceNotFoundException("Category", "ID", categoryId);
                });
    }

    @Override
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        log.debug("Attempting to create product with title: {}", request.getTitle());

        // Kiểm tra ISBN trùng lặp (nếu ISBN được cung cấp)
        if (StringUtils.hasText(request.getIsbn()) && productRepository.existsByIsbn(request.getIsbn())) {
            log.warn("Product creation failed: ISBN '{}' already exists.", request.getIsbn());
            throw new DuplicateResourceException("Product with ISBN '" + request.getIsbn() + "' already exists.");
        }

        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setAuthor(request.getAuthor());
        product.setIsbn(request.getIsbn());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setPublishedDate(request.getPublishedDate());
        
        // Tìm category chính theo ID (nếu có)
        if (request.getCategoryId() != null) {
            Category category = findCategoryById(request.getCategoryId());
            product.setCategory(category);
        }
        
        // Xử lý nhiều categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : request.getCategoryIds()) {
                Category category = findCategoryById(categoryId);
                categories.add(category);
            }
            product.setCategories(categories);
            
            // Đặt danh mục đầu tiên làm danh mục chính nếu chưa có
            if (product.getCategory() == null && !categories.isEmpty()) {
                product.setCategory(categories.iterator().next());
            }
        } 
        // Đảm bảo danh mục chính cũng nằm trong danh sách categories
        else if (product.getCategory() != null) {
            product.getCategories().add(product.getCategory());
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return mapToProductDTO(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID id) {
        log.debug("Fetching product with ID: {}", id);
        // Lấy Product (nên JOIN FETCH Category nếu cần tối ưu)
        Product product = productRepository.findById(id) // Tạm thời chưa fetch category
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", id));

        // Lấy review stats cho sản phẩm này
        // findReviewStatsByProductIdsProjection trả về List, lấy phần tử đầu nếu có
        ReviewStats stats = reviewRepository.findReviewStatsByProductIdsProjection(Collections.singletonList(id))
                .stream().findFirst().orElse(null);

        // Map Product và Stats sang DTO
        return mapToProductDTO(product, stats);
    }
    // Phương thức chung để xử lý Page<Product> và thêm Review Stats
    private Page<ProductDTO> mapProductPageToDtoWithStats(Page<Product> productPage) {
        List<Product> productsOnPage = productPage.getContent();
        Map<UUID, ReviewStats> statsMap = Collections.emptyMap(); // Khởi tạo map rỗng

        if (!productsOnPage.isEmpty()) {
            // Lấy danh sách Product IDs
            List<UUID> productIds = productsOnPage.stream().map(Product::getId).collect(Collectors.toList());

            // Query 1 lần để lấy stats cho tất cả product IDs trên trang
            List<ReviewStats> reviewStatsList = reviewRepository.findReviewStatsByProductIdsProjection(productIds);

            // Chuyển List<ReviewStats> thành Map<ProductId, ReviewStats> để dễ tra cứu
            statsMap = reviewStatsList.stream()
                    .collect(Collectors.toMap(ReviewStats::getProductId, Function.identity()));
        }

        // Map Page<Product> sang Page<ProductDTO>, truyền ReviewStats tương ứng vào hàm map
        // Cần final hoặc effectively final để dùng trong lambda
        final Map<UUID, ReviewStats> finalStatsMap = statsMap;
        return productPage.map(product -> mapToProductDTO(product, finalStatsMap.get(product.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products with pagination: {}", pageable);
        // findById trả về Page<Product>, cần map sang Page<ProductDTO>
        Page<Product> productPage = productRepository.findAll(pageable);
        // Sử dụng map của Page để chuyển đổi content
        return productPage.map(this::mapToProductDTO);
        // Lưu ý: Vẫn có thể bị N+1 cho category. Cần JOIN FETCH trong query `findAll` nếu muốn tối ưu.
        // Ví dụ sửa ProductRepository:
        // @Query(value = "SELECT p FROM Product p JOIN FETCH p.category",
        //        countQuery = "SELECT count(p) FROM Product p")
        // Page<Product> findAllWithCategory(Pageable pageable);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> filterProducts(Long categoryId, String keyword, BigDecimal minPrice, BigDecimal maxPrice, Boolean inStockOnly, String author, Pageable pageable) {
        log.debug("Filtering products with criteria - ..."); // Log đầy đủ

        Specification<Product> spec = ProductSpecification.buildSpecification(
                keyword, categoryId, minPrice, maxPrice, inStockOnly, author
        );
        // Query lấy Page<Product>
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // Xử lý thêm stats và map sang DTO
        return mapProductPageToDtoWithStats(productPage);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("Fetching products for category ID: {} with pagination: {}", categoryId, pageable);
        // Gọi lại filterProducts để tận dụng logic và xử lý stats
        return filterProducts(categoryId, null, null, null, null, null, pageable);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(UUID id, UpdateProductRequest request) {
        log.debug("Attempting to update product with ID: {}", id);

        // Tìm sản phẩm hiện tại, không có sẽ ném ResourceNotFoundException
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", id));

        // Kiểm tra ISBN trùng lặp (nếu ISBN được cung cấp và đã thay đổi)
        if (StringUtils.hasText(request.getIsbn()) && !request.getIsbn().equals(product.getIsbn())
                && productRepository.existsByIsbn(request.getIsbn())) {
            log.warn("Product update failed: ISBN '{}' already exists.", request.getIsbn());
            throw new DuplicateResourceException("Product with ISBN '" + request.getIsbn() + "' already exists.");
        }

        // Cập nhật thông tin sản phẩm
        product.setTitle(request.getTitle());
        product.setAuthor(request.getAuthor());
        product.setIsbn(request.getIsbn());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setPublishedDate(request.getPublishedDate());

        // Cập nhật category chính nếu có thay đổi
        if (request.getCategoryId() != null) {
            Category category = findCategoryById(request.getCategoryId());
            product.setCategory(category);
        }
        
        // Cập nhật danh sách categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            // Xóa tất cả categories hiện tại
            product.getCategories().clear();
            
            // Thêm categories mới
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : request.getCategoryIds()) {
                Category category = findCategoryById(categoryId);
                categories.add(category);
            }
            product.setCategories(categories);
            
            // Đặt danh mục đầu tiên làm danh mục chính nếu chưa có
            if (product.getCategory() == null && !categories.isEmpty()) {
                product.setCategory(categories.iterator().next());
            }
        } 
        // Đảm bảo danh mục chính cũng nằm trong danh sách categories
        else if (product.getCategory() != null) {
            product.getCategories().add(product.getCategory());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        return mapToProductDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        log.debug("Attempting to delete product with ID: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Product deletion failed: Product not found with ID: {}", id);
            throw new ResourceNotFoundException("Product", "ID", id);
        }
        // TODO: Cần kiểm tra xem sản phẩm có nằm trong đơn hàng nào không trước khi xóa? (Logic phức tạp hơn)
        // Nếu có ràng buộc khóa ngoại từ OrderItem đến Product, DB sẽ tự chặn xóa.
        // Nếu không, cần kiểm tra thủ công.
        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        return filterProducts(null, keyword, null, null, null, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPurchasedProduct(Long userId, UUID productId) {
        log.debug("Checking if user ID {} has purchased product ID {}", userId, productId);
        
        // Kiểm tra sản phẩm tồn tại
        if (!productRepository.existsById(productId)) {
            log.warn("Product with ID {} does not exist", productId);
            return false;
        }
        
        // Kiểm tra người dùng đã mua và nhận hàng thành công
        boolean hasPurchased = orderRepository.existsByUserIdAndItemsProductIdAndStatusDelivered(
            userId, 
            productId, 
            OrderStatus.DELIVERED
        );
        
        log.debug("User ID {} has purchased product ID {}: {}", userId, productId, hasPurchased);
        return hasPurchased;
    }
}