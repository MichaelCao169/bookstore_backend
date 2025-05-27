package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(name = "last_message_content")
    private String lastMessageContent;

    @Column(name = "last_message_timestamp")
    private LocalDateTime lastMessageTimestamp;

    @Column(name = "unread_count_customer", nullable = false)
    private Integer unreadCountCustomer = 0;

    @Column(name = "unread_count_admin", nullable = false)
    private Integer unreadCountAdmin = 0;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();

    public enum ConversationStatus {
        ACTIVE, CLOSED, ARCHIVED
    }

    // Helper methods
    public void updateLastMessage(String content, LocalDateTime timestamp) {
        this.lastMessageContent = content;
        this.lastMessageTimestamp = timestamp;
    }

    public void incrementUnreadCountForCustomer() {
        this.unreadCountCustomer++;
    }

    public void incrementUnreadCountForAdmin() {
        this.unreadCountAdmin++;
    }

    public void clearUnreadCountForCustomer() {
        this.unreadCountCustomer = 0;
    }

    public void clearUnreadCountForAdmin() {
        this.unreadCountAdmin = 0;
    }
}
