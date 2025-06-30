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
            // Đếm tổng số sản phẩm
            long totalProducts = productRepository.count();
            
            // Đếm tổng số đơn hàng hợp lệ (không bao gồm đơn hàng bị hủy và đơn hàng thất bại)
            long totalOrders = orderRepository.countValidOrders() != null 
                ? orderRepository.countValidOrders() 
                : 0L;
            
            // Đếm tổng số người dùng
            long totalUsers = userRepository.count();
            
            // Lấy tổng doanh thu từ các đơn hàng đã hoàn thành (không bao gồm đơn hàng bị hủy và đơn hàng thất bại)
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
            // Lấy ra các sản phẩm bán chạy nhất
            // để lấy ra các sản phẩm được sắp xếp theo số lượng bán
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
            // Trả về danh sách rỗng nếu có lỗi
            return new ArrayList<>();
        }
    }
} 