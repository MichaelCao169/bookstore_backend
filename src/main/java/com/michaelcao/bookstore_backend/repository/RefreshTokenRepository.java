package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.RefreshToken;
import com.michaelcao.bookstore_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user u JOIN FETCH u.roles WHERE rt.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);
    
    @Modifying
    int deleteByUser(User user);
}