// src/main/java/com/michaelcao/bookstore_backend/repository/PasswordResetTokenRepository.java
package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.PasswordResetToken;
import com.michaelcao.bookstore_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByUser(User user); // Để xóa token cũ khi tạo token mới
}