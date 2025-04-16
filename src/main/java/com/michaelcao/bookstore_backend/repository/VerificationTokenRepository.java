package com.michaelcao.bookstore_backend.repository;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user);
    void deleteByUser(User user); // Added for cleaning up old tokens
}