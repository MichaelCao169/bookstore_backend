package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.Conversation;
import com.michaelcao.bookstore_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    // Find conversation between customer and any admin
    @Query("SELECT c FROM Conversation c WHERE c.customer.id = :customerId AND c.status = 'ACTIVE'")
    Optional<Conversation> findActiveConversationByCustomerId(@Param("customerId") Long customerId);
    
    // Find all conversations for admin (ordered by last message timestamp)
    @Query("SELECT c FROM Conversation c WHERE c.status = 'ACTIVE' ORDER BY c.lastMessageTimestamp DESC")
    List<Conversation> findAllActiveConversationsForAdmin();
    
    // Find conversations assigned to specific admin
    @Query("SELECT c FROM Conversation c WHERE c.admin.id = :adminId AND c.status = 'ACTIVE' ORDER BY c.lastMessageTimestamp DESC")
    List<Conversation> findConversationsByAdminId(@Param("adminId") Long adminId);
    
    // Count unread conversations for admin
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.unreadCountAdmin > 0 AND c.status = 'ACTIVE'")
    Long countUnreadConversationsForAdmin();
    
    // Find conversation by customer and admin
    Optional<Conversation> findByCustomerAndAdmin(User customer, User admin);
    
    // Find conversation by customer (regardless of admin)
    Optional<Conversation> findByCustomer(User customer);
}
