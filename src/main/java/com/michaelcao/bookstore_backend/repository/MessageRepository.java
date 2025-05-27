package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.Conversation;
import com.michaelcao.bookstore_backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Find messages by conversation (ordered by creation time)
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
    
    // Find messages by conversation with pagination
    Page<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);
      // Count unread messages for customer in a conversation
    @Query("SELECT COUNT(m) FROM Message m JOIN m.sender.roles r WHERE m.conversation.id = :conversationId AND m.isReadByCustomer = false AND r.name = 'ROLE_ADMIN'")
    Long countUnreadMessagesByCustomerInConversation(@Param("conversationId") Long conversationId);
    
    // Count unread messages for admin in a conversation
    @Query("SELECT COUNT(m) FROM Message m JOIN m.sender.roles r WHERE m.conversation.id = :conversationId AND m.isReadByAdmin = false AND r.name = 'ROLE_CUSTOMER'")
    Long countUnreadMessagesByAdminInConversation(@Param("conversationId") Long conversationId);
    
    // Mark messages as read by customer
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isReadByCustomer = true WHERE m.conversation.id = :conversationId AND EXISTS (SELECT 1 FROM m.sender.roles r WHERE r.name = 'ROLE_ADMIN')")
    void markMessagesAsReadByCustomer(@Param("conversationId") Long conversationId);
    
    // Mark messages as read by admin
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isReadByAdmin = true WHERE m.conversation.id = :conversationId AND EXISTS (SELECT 1 FROM m.sender.roles r WHERE r.name = 'ROLE_CUSTOMER')")
    void markMessagesAsReadByAdmin(@Param("conversationId") Long conversationId);
    
    // Find latest message in conversation
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC LIMIT 1")
    Message findLatestMessageInConversation(@Param("conversationId") Long conversationId);
}
