package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.dto.category.CategoryDTO;
import com.michaelcao.bookstore_backend.dto.category.CreateCategoryRequest;
import com.michaelcao.bookstore_backend.dto.category.UpdateCategoryRequest;
import com.michaelcao.bookstore_backend.entity.Category;
import com.michaelcao.bookstore_backend.exception.DuplicateResourceException; // *** TẠO EXCEPTION NÀY ***
import com.michaelcao.bookstore_backend.exception.OperationNotAllowedException; // *** TẠO EXCEPTION NÀY ***
import com.michaelcao.bookstore_backend.exception.ResourceNotFoundException;
import com.michaelcao.bookstore_backend.repository.CategoryRepository;
import com.michaelcao.bookstore_backend.repository.ProductRepository; // Inject để kiểm tra product trước khi xóa category
import com.michaelcao.bookstore_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Quan trọng cho update/delete

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok: Tạo constructor cho các field final
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository; // Inject ProductRepository

    // --- Helper method for mapping ---
    private CategoryDTO mapToCategoryDTO(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

    @Override
    @Transactional // Đảm bảo atomicity
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        log.debug("Attempting to create category with name: {}", request.getName());
        // Kiểm tra tên trùng lặp (không phân biệt hoa thường)
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Category creation failed: Name '{}' already exists.", request.getName());
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists.");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return mapToCategoryDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public CategoryDTO getCategoryById(Long id) {
        log.debug("Fetching category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", id);
                    return new ResourceNotFoundException("Category", "ID", id);
                });
        return mapToCategoryDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.debug("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        // Sử dụng Stream API để map danh sách Entity sang DTO
        return categories.stream()
                .map(this::mapToCategoryDTO) // Tham chiếu đến phương thức mapToCategoryDTO
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, UpdateCategoryRequest request) {
        log.debug("Attempting to update category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category update failed: Category not found with ID: {}", id);
                    return new ResourceNotFoundException("Category", "ID", id);
                });

        // Kiểm tra tên trùng lặp (không phân biệt hoa thường), nhưng loại trừ chính category đang update
        categoryRepository.findByNameIgnoreCase(request.getName())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getId().equals(id)) { // Nếu tìm thấy category khác có cùng tên
                        log.warn("Category update failed: Name '{}' already exists for category ID: {}", request.getName(), existingCategory.getId());
                        throw new DuplicateResourceException("Category name '" + request.getName() + "' is already in use.");
                    }
                });

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());
        return mapToCategoryDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.debug("Attempting to delete category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category deletion failed: Category not found with ID: {}", id);
                    return new ResourceNotFoundException("Category", "ID", id);
                });

        // Kiểm tra xem có sản phẩm nào thuộc danh mục này không
        // Dùng count query để hiệu quả hơn là lấy cả danh sách
        long productCount = productRepository.countByCategoryId(id); // *** CẦN THÊM METHOD NÀY VÀO ProductRepository ***
        if (productCount > 0) {
            log.warn("Category deletion failed: Category ID {} still contains {} products.", id, productCount);
            throw new OperationNotAllowedException("Cannot delete category: It contains existing products. Please move or delete products first.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
    }
}