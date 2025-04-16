package com.michaelcao.bookstore_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService; // Giữ lại import này
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor // Bạn vẫn có thể giữ lại nếu có dependency khác cho config class, nhưng không bắt buộc cho các @Bean bên dưới nữa nếu chúng nhận tham số
public class ApplicationConfig {

    // Không cần field này nữa nếu bạn truyền dependency qua tham số @Bean
    // private final UserDetailsService userDetailsService;

    // Bean này không phụ thuộc vào field nào của ApplicationConfig
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Sửa ở đây: Truyền UserDetailsService và PasswordEncoder làm tham số
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Sử dụng tham số được inject thay vì field của class
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // Bean này nhận AuthenticationConfiguration làm tham số, đã đúng
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
