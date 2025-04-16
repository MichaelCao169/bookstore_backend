package com.michaelcao.bookstore_backend.service.impl;

// Import các dependency cần thiết
import com.michaelcao.bookstore_backend.config.DataInitializer;
import com.michaelcao.bookstore_backend.dto.auth.LoginRequest;
import com.michaelcao.bookstore_backend.dto.auth.RegisterRequest;
import com.michaelcao.bookstore_backend.entity.*;
import com.michaelcao.bookstore_backend.exception.EmailAlreadyExistsException;
import com.michaelcao.bookstore_backend.exception.InvalidTokenException;
import com.michaelcao.bookstore_backend.exception.ResourceNotFoundException;
import com.michaelcao.bookstore_backend.repository.PasswordResetTokenRepository;
import com.michaelcao.bookstore_backend.repository.RoleRepository;
import com.michaelcao.bookstore_backend.repository.UserRepository;
import com.michaelcao.bookstore_backend.security.jwt.JwtUtil;
import com.michaelcao.bookstore_backend.service.AuthService;
import com.michaelcao.bookstore_backend.dto.auth.AuthResponse;
import org.springframework.security.core.GrantedAuthority;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
// Import các service/util khác khi cần (JwtUtil, RefreshTokenService, EmailService)
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.michaelcao.bookstore_backend.repository.VerificationTokenRepository;
import com.michaelcao.bookstore_backend.service.EmailService;
import java.util.UUID;

import com.michaelcao.bookstore_backend.service.RefreshTokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
     private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    @Transactional
    public ResponseEntity<String> register(RegisterRequest registerRequest) {
        log.debug("Attempting to register user with email: {}", registerRequest.getEmail());
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Registration failed: Email {} already exists.", registerRequest.getEmail());
            throw new EmailAlreadyExistsException("Error: Email is already taken!");
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .enabled(false) // Set to false, require email verification
                .build();

        Role customerRole = roleRepository.findByName(DataInitializer.ROLE_CUSTOMER)
                .orElseThrow(() -> {
                    log.error("CRITICAL: Role {} not found during registration!", DataInitializer.ROLE_CUSTOMER);
                    return new ResourceNotFoundException("Error: Default role not found."); // More specific exception
                });
        user.addRole(customerRole);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());

        // --- TODO: SEND VERIFICATION EMAIL ---
        try {
            String token = generateVerificationToken(savedUser);
            // TODO: Cần tạo link xác thực hoàn chỉnh (ví dụ: bao gồm domain frontend)
            // String verificationUrl = "http://localhost:3000/verify-email?token=" + token; // Ví dụ link Frontend
            String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token; // Tạm dùng link Backend để test
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getName(), verificationUrl);
            log.info("Verification email initiated for {}", savedUser.getEmail());
        } catch (Exception e) {
            // Quan trọng: Việc gửi mail thất bại không nên làm rollback transaction đăng ký user
            // Chỉ log lại lỗi để xử lý sau (ví dụ: gửi lại, thông báo admin...)
            log.error("Failed to send verification email to {}: {}", savedUser.getEmail(), e.getMessage());
            // Có thể ném một exception khác không rollback hoặc không ném gì cả
        }


        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Registration successful! Please check your email to verify your account.");
    }

    @Override
    public ResponseEntity<?> login(LoginRequest loginRequest) {
        log.debug("Attempting to login user with email: {}", loginRequest.getEmail());
        try {
            // 1. Xác thực bằng AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // 2. Nếu xác thực thành công, đặt vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Lấy thông tin UserDetails
            User userDetails = (User) authentication.getPrincipal();
            log.info("User logged in successfully: {}", userDetails.getEmail());

            // *** KHÔNG CẦN KIỂM TRA userDetails.isEnabled() Ở ĐÂY NỮA ***
            // Vì nó đã được xử lý bởi DisabledException catch block

            // --- TODO: GENERATE JWT and REFRESH TOKEN ---
             String accessToken = jwtUtil.generateToken(userDetails);
            // RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
            // *** TẠO REFRESH TOKEN VÀ COOKIE ***
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
            ResponseCookie jwtRefreshCookie = refreshTokenService.generateRefreshCookie(refreshToken.getToken());
            // Get roles as Strings
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Build AuthResponse
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .userId(userDetails.getId())
                    .email(userDetails.getEmail())
                    .roles(roles)
                    .build();

            // Return AuthResponse in the body
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString()) // Set cookie vào header
                    .body(authResponse);


        } catch (DisabledException e) { // *** CATCH RIÊNG DisabledException ***
            // 4a. Bắt lỗi TÀI KHOẢN BỊ VÔ HIỆU HÓA (chưa verify)
            log.warn("Login failed: Account {} is disabled (not verified).", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Account not verified. Please check your email."); // <-- Thông báo lỗi chính xác

        } catch (BadCredentialsException e) { // *** CATCH RIÊNG BadCredentialsException ***
            // 4b. Bắt lỗi SAI MẬT KHẨU/EMAIL
            log.warn("Login failed for email {}: Invalid credentials.", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Invalid email or password."); // <-- Thông báo lỗi chung

        } catch (AuthenticationException e) { // *** CATCH các AuthenticationException khác ***
            // 4c. Bắt các lỗi xác thực khác (tài khoản bị khóa, hết hạn, v.v.)
            log.warn("Authentication failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Authentication failed."); // Thông báo chung hơn

        } catch (Exception e) {
            // 5. Bắt các lỗi không mong muốn khác
            log.error("An unexpected error occurred during login for email {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred during login.");
        }
    }
    @Override
    public void forgotPassword(String email) {
        log.debug("Processing forgot password request for email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Xóa token cũ nếu có
            passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);

            // Tạo token mới
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user);
            passwordResetTokenRepository.save(resetToken);
            log.debug("Generated password reset token for user {}: {}", email, token);

            // Tạo link reset (nên trỏ đến trang frontend)
            // String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String resetUrl = "http://localhost:8080/api/auth/reset-password?token=" + token; // Tạm dùng link backend để test copy/paste token
            // Link này thực ra không dùng để click, chỉ để gửi token qua email

            // Gửi email
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetUrl); // Gửi link chứa token
                log.info("Password reset email initiated for {}", email);
            } catch (Exception e) {
                log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
                // Không nên ném lỗi ra ngoài để tránh lộ thông tin email
            }
        } else {
            // Không tìm thấy email -> không làm gì cả, chỉ log (để tránh lộ thông tin)
            log.warn("Forgot password request for non-existent email: {}", email);
        }
        // Không trả về lỗi dù email có tồn tại hay không
    }

    // *** IMPLEMENT resetPassword ***
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.debug("Processing password reset for token: {}", token);

        // 1. Tìm token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or non-existent password reset token."));

        // 2. Kiểm tra hết hạn
        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Password reset token has expired.");
        }

        // 3. Lấy user
        User user = resetToken.getUser();
        if (user == null) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Invalid token: No associated user found.");
        }

        // 4. Hash và cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successfully for user: {}", user.getEmail());

        // 5. Xóa token đã sử dụng
        passwordResetTokenRepository.delete(resetToken);
        log.debug("Password reset token deleted: {}", token);

        // 6. (Optional) Xóa các refresh token cũ để buộc đăng nhập lại
        // refreshTokenService.deleteByUserId(user.getId());
        // log.info("Old refresh tokens deleted for user: {}", user.getEmail());
    }
    // *** HÀM PRIVATE TẠO VÀ LƯU TOKEN XÁC THỰC ***
    private String generateVerificationToken(User user) {
        // Xóa token cũ (nếu có) của user này để đảm bảo chỉ có 1 token active
        verificationTokenRepository.findByUser(user).ifPresent(verificationTokenRepository::delete);

        String token = UUID.randomUUID().toString(); // Tạo token ngẫu nhiên
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);
        log.debug("Generated verification token for user {}: {}", user.getEmail(), token);
        return token;
    }

    @Override
    @Transactional // Cần transaction vì cập nhật user và xóa token
    public void verifyAccount(String token) {
        log.debug("Verifying account with token: {}", token);

        // 1. Tìm token trong DB
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token."));

        // 2. Kiểm tra token hết hạn
        if (verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken); // Xóa token hết hạn
            throw new InvalidTokenException("Verification token has expired.");
        }

        // 3. Lấy user từ token
        User user = verificationToken.getUser();
        if (user == null) { // Kiểm tra phòng trường hợp data bất thường
            verificationTokenRepository.delete(verificationToken);
            throw new InvalidTokenException("Invalid token: No associated user found.");
        }

        // 4. Kích hoạt tài khoản user
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Account enabled successfully for user: {}", user.getEmail());

        // 5. Xóa token đã sử dụng khỏi DB
        verificationTokenRepository.delete(verificationToken);
        log.debug("Verification token deleted: {}", token);
    }
    //  TODO: Implement other methods: verifyAccount, forgotPassword, resetPassword, refreshToken
    // private String generateVerificationToken(User user) { ... }

}