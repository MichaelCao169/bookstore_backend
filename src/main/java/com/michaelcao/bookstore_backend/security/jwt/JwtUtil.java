package com.michaelcao.bookstore_backend.security.jwt;

import com.michaelcao.bookstore_backend.entity.User; // Import User entity
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // Specific exception for signature issues
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority; // For roles
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List; // For roles list
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors; // For roles stream

@Component
@Slf4j // Added logger
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpiration;

    // Trích xuất username (email) từ token
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject); // Subject thường chứa username
        } catch (Exception e) {
            log.error("Could not extract username from token: {}", e.getMessage());
            return null;
        }
    }

    // Trích xuất một claim cụ thể từ token bằng cách sử dụng một hàm resolver
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        return claimsResolver.apply(claims);
    }

    // Tạo access token cho UserDetails
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Thêm roles vào claims
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        extraClaims.put("roles", roles);

        // Nếu userDetails là instance của User entity, bạn có thể thêm các claim khác
        if (userDetails instanceof User customUser) {
            extraClaims.put("userId", customUser.getId());
            extraClaims.put("name", customUser.getName());
        }

        return generateToken(extraClaims, userDetails);
    }

    // Tạo access token với các claims bổ sung
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, accessTokenExpiration);
    }

    // Phương thức chung để xây dựng token
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims) // Đặt các claims tùy chỉnh trước
                .setSubject(userDetails.getUsername()) // Subject là username (email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Thời gian phát hành
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Thời gian hết hạn
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Ký token
                .compact(); // Build thành chuỗi JWT
    }

    // Kiểm tra xem token có hợp lệ với UserDetails không
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username != null && username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) { // Catch any other potential issues during validation
            log.error("Error validating JWT token: {}", e.getMessage());
            return false;
        }
    }

    // Kiểm tra xem token đã hết hạn chưa
    private boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        if (expirationDate == null) return true; // Cannot determine expiration, treat as expired
        return expirationDate.before(new Date());
    }

    // Trích xuất thời gian hết hạn từ token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Trích xuất tất cả claims từ token
    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .setSigningKey(getSignInKey()) // Cung cấp khóa để xác thực chữ ký
                    .build()
                    .parseClaimsJws(token) // Phân tích và xác thực token
                    .getBody(); // Lấy phần payload (claims)
        } catch (Exception e) { // Catch various JWT exceptions
            log.debug("Could not extract claims from token: {}", e.getMessage()); // Use debug for potentially frequent errors
            return null; // Indicate failure to parse/validate
        }
    }

    // Lấy khóa ký từ secret key (đã được mã hóa Base64)
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}