package com.michaelcao.bookstore_backend.config;

import com.michaelcao.bookstore_backend.security.jwt.JwtAuthenticationFilter; 
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private static final String[] PUBLIC_MATCHERS = {
            "/api/auth/**",
            "/api/auth/verify-email/**",
            "/api/reset-password/**",
            "/api/test/hello-public",
            "/api/uploads/**",
            "/ws/**", 
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // vô hiệu hóa CSRF vì đây là API RESTful
            .csrf(AbstractHttpConfigurer::disable)
            
            // thiết lập CORS
            .cors(cors -> {})
            
            // Config authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers(PUBLIC_MATCHERS).permitAll()
                
                // Public GET endpoints for products and categories
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                
                // Customer endpoints
                .requestMatchers("/api/cart/**").hasRole("CUSTOMER")
                .requestMatchers("/api/orders/**").hasRole("CUSTOMER")
                .requestMatchers("/api/wishlist/**").hasRole("CUSTOMER")
                .requestMatchers("/api/ai-chat/**").authenticated()
                // Admin endpoints
                .requestMatchers("/api/products/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Chat endpoints
                .requestMatchers("/api/chat/**").authenticated()
                
                // User profile 
                .requestMatchers("/api/profile/**").authenticated()
                
                // Còn lại các request đều phải đăng nhập
                .anyRequest().authenticated()
            )
            
            //  Session management for REST APIs
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            //  authentication provider
            .authenticationProvider(authenticationProvider)
            
            // Thêm JWT filter trước UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
