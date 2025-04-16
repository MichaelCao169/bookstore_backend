package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.category.CategoryDTO;
import com.michaelcao.bookstore_backend.dto.category.CreateCategoryRequest;
import com.michaelcao.bookstore_backend.dto.category.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {

    /**
     * Tạo một danh mục mới.
     * @param request DTO chứa thông tin danh mục mới.
     * @return CategoryDTO của danh mục vừa tạo.
     * @throws com.michaelcao.bookstore_backend.exception.DuplicateResourceException Nếu tên danh mục đã tồn tại.
     */
    CategoryDTO createCategory(CreateCategoryRequest request);

    /**
     * Lấy thông tin chi tiết của một danh mục theo ID.
     * @param id ID của danh mục.
     * @return CategoryDTO của danh mục.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy danh mục.
     */
    CategoryDTO getCategoryById(Long id);

    /**
     * Lấy danh sách tất cả các danh mục.
     * @return List các CategoryDTO.
     */
    List<CategoryDTO> getAllCategories();

    /**
     * Cập nhật thông tin một danh mục.
     * @param id ID của danh mục cần cập nhật.
     * @param request DTO chứa thông tin cập nhật.
     * @return CategoryDTO của danh mục sau khi cập nhật.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy danh mục.
     * @throws com.michaelcao.bookstore_backend.exception.DuplicateResourceException Nếu tên danh mục mới trùng với tên khác (ngoại trừ chính nó).
     */
    CategoryDTO updateCategory(Long id, UpdateCategoryRequest request);

    /**
     * Xóa một danh mục.
     * @param id ID của danh mục cần xóa.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy danh mục.
     * @throws com.michaelcao.bookstore_backend.exception.OperationNotAllowedException Nếu danh mục còn chứa sản phẩm.
     */
    void deleteCategory(Long id);
}