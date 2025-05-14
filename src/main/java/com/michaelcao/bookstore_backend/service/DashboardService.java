package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.dashboard.DashboardStatsDTO;
import com.michaelcao.bookstore_backend.dto.product.ProductDTO;

import java.util.List;

public interface DashboardService {
    /**
     * Get dashboard statistics overview
     * @return DashboardStatsDTO with counts and totals
     */
    DashboardStatsDTO getDashboardStats();
    
    /**
     * Get top selling products
     * @param limit Number of products to return
     * @return List of ProductDTO ordered by sales count
     */
    List<ProductDTO> getTopSellingProducts(int limit);
} 