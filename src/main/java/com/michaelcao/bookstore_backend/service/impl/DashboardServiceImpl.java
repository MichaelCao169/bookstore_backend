package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.dto.dashboard.DashboardStatsDTO;
import com.michaelcao.bookstore_backend.dto.product.ProductDTO;
import com.michaelcao.bookstore_backend.repository.OrderRepository;
import com.michaelcao.bookstore_backend.repository.ProductRepository;
import com.michaelcao.bookstore_backend.repository.UserRepository;
import com.michaelcao.bookstore_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        log.debug("Fetching dashboard statistics");
        
        try {
            // Count total products
            long totalProducts = productRepository.count();
            
            // Count total valid orders (excluding cancelled and failed orders)
            long totalOrders = orderRepository.countValidOrders() != null 
                ? orderRepository.countValidOrders() 
                : 0L;
            
            // Count total users
            long totalUsers = userRepository.count();
            
            // Get total revenue from completed orders only (excluding cancelled and failed orders)
            BigDecimal totalRevenue = orderRepository.getTotalRevenue() != null 
                ? orderRepository.getTotalRevenue() 
                : BigDecimal.ZERO;
            
            return DashboardStatsDTO.builder()
                    .totalProducts(totalProducts)
                    .totalOrders(totalOrders)
                    .totalUsers(totalUsers)
                    .totalRevenue(totalRevenue)
                    .build();
        } catch (Exception e) {
            log.error("Error getting dashboard stats", e);
            // Return default stats if there's an error
            return DashboardStatsDTO.builder()
                    .totalProducts(0L)
                    .totalOrders(0L)
                    .totalUsers(0L)
                    .totalRevenue(BigDecimal.ZERO)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getTopSellingProducts(int limit) {
        log.debug("Fetching top {} selling products", limit);
          try {
            // Get top products by sales count
            // This is a mock implementation - you'd typically query your database
            // to get products ordered by sales count
            return productRepository.findAllByOrderBySoldCountDesc()
                    .stream()
                    .limit(limit)
                    .map(product -> ProductDTO.builder()
                            .productId(product.getProductId())
                            .title(product.getTitle())
                            .author(product.getAuthor())
                            .description(product.getDescription())
                            .currentPrice(product.getCurrentPrice())
                            .quantity(product.getQuantity())
                            .coverLink(product.getCoverLink())
                            .soldCount(product.getSoldCount() != null ? product.getSoldCount() : 0)
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting top selling products", e);
            // Return empty list if there's an error
            return new ArrayList<>();
        }
    }
} 