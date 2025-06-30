package com.michaelcao.bookstore_backend.security.jwt; // Ensure correct package

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Lấy JWT từ header Authorization
            String jwt = extractJwtFromRequest(request);
            
            // Nếu không có token hoặc định dạng không hợp lệ, tiếp tục filter chain
            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // Lấy username từ JWT
            String username = jwtUtil.extractUsername(jwt);
            
            // Kiểm tra username và kiểm tra xem có authentication trong context chưa
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Tải thông tin user
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Kiểm tra token
                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    // Tạo authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Không cần credentials cho JWT auth
                            userDetails.getAuthorities()
                    );
                    
                    // Đặt chi tiết
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Cập nhật security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Successfully authenticated user '{}' via JWT", username);
                } else {
                    log.debug("Invalid JWT token for user '{}'", username);
                }
            }
        } catch (Exception e) {
            log.error("Could not authenticate user with JWT token", e);
            // Không ném exception, chỉ tiếp tục filter chain
        }
        
        // tiếp tục filter chain
        filterChain.doFilter(request, response);
    }
    
    /**
     * Lấy JWT từ header Authorization
     * @param request The HTTP request
     * @return JWT token hoặc null nếu không tìm thấy hoặc không hợp lệ
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}