package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.chat.ConversationResponse;
import com.michaelcao.bookstore_backend.dto.chat.MessageResponse;
import com.michaelcao.bookstore_backend.dto.chat.SendMessageRequest;
import com.michaelcao.bookstore_backend.entity.Conversation;
import com.michaelcao.bookstore_backend.entity.Message;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.repository.ConversationRepository;
import com.michaelcao.bookstore_backend.repository.MessageRepository;
import com.michaelcao.bookstore_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Customer sends message to admin
    public MessageResponse sendMessageFromCustomer(Long customerId, SendMessageRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Find or create conversation
        Conversation conversation = conversationRepository.findActiveConversationByCustomerId(customerId)
                .orElseGet(() -> createNewConversation(customer));
        
        // Create message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(customer);
        message.setContent(request.getContent());
        message.setMessageType(Message.MessageType.valueOf(request.getMessageType()));
        message.setIsReadByCustomer(true); // Customer has read their own message
        message.setIsReadByAdmin(false);
        
        message = messageRepository.save(message);
        
        // Update conversation
        conversation.updateLastMessage(request.getContent(), message.getCreatedAt());
        conversation.incrementUnreadCountForAdmin();
        conversationRepository.save(conversation);
        
        MessageResponse response = convertToMessageResponse(message);
        
        // Send real-time notification to admin
        messagingTemplate.convertAndSend("/topic/admin/messages", response);
        
        return response;
    }
    
    // Admin sends message to customer
    public MessageResponse sendMessageFromAdmin(Long adminId, Long conversationId, SendMessageRequest request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Assign admin to conversation if not assigned
        if (conversation.getAdmin() == null) {
            conversation.setAdmin(admin);
        }
        
        // Create message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(admin);
        message.setContent(request.getContent());
        message.setMessageType(Message.MessageType.valueOf(request.getMessageType()));
        message.setIsReadByAdmin(true); // Admin has read their own message
        message.setIsReadByCustomer(false);
        
        message = messageRepository.save(message);
        
        // Update conversation
        conversation.updateLastMessage(request.getContent(), message.getCreatedAt());
        conversation.incrementUnreadCountForCustomer();
        conversationRepository.save(conversation);
        
        MessageResponse response = convertToMessageResponse(message);
        
        // Send real-time notification to customer
        messagingTemplate.convertAndSend("/topic/customer/" + conversation.getCustomer().getId() + "/messages", response);
        
        return response;
    }
    
    // Get conversation for customer
    public ConversationResponse getCustomerConversation(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Conversation conversation = conversationRepository.findActiveConversationByCustomerId(customerId)
                .orElseGet(() -> createNewConversation(customer));
        
        return convertToConversationResponse(conversation, true);
    }
    
    // Get messages in conversation with pagination
    public List<MessageResponse> getConversationMessages(Long conversationId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagesPage = messageRepository.findByConversationOrderByCreatedAtDesc(conversation, pageable);
        
        return messagesPage.getContent().stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
    }
    
    // Get all conversations for admin
    public List<ConversationResponse> getAdminConversations() {
        List<Conversation> conversations = conversationRepository.findAllActiveConversationsForAdmin();
        return conversations.stream()
                .map(conv -> convertToConversationResponse(conv, false))
                .collect(Collectors.toList());
    }
    
    // Mark messages as read by customer
    public void markMessagesAsReadByCustomer(Long customerId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        if (!conversation.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Unauthorized access to conversation");
        }
        
        messageRepository.markMessagesAsReadByCustomer(conversationId);
        conversation.clearUnreadCountForCustomer();
        conversationRepository.save(conversation);
    }
    
    // Mark messages as read by admin
    public void markMessagesAsReadByAdmin(Long conversationId) {
        messageRepository.markMessagesAsReadByAdmin(conversationId);
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.clearUnreadCountForAdmin();
        conversationRepository.save(conversation);
    }
    
    // Helper methods
    private Conversation createNewConversation(User customer) {
        Conversation conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setStatus(Conversation.ConversationStatus.ACTIVE);
        conversation.setLastMessageTimestamp(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }
      private MessageResponse convertToMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setConversationId(message.getConversation().getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getName());
        response.setSenderAvatar(message.getSender().getAvatarUrl());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType().toString());
        response.setIsReadByCustomer(message.getIsReadByCustomer());
        response.setIsReadByAdmin(message.getIsReadByAdmin());
        response.setCreatedAt(message.getCreatedAt());
        response.setIsFromAdmin(message.isFromAdmin());
        return response;
    }
      private ConversationResponse convertToConversationResponse(Conversation conversation, boolean includeMessages) {
        ConversationResponse response = new ConversationResponse();
        response.setId(conversation.getId());
        response.setCustomerId(conversation.getCustomer().getId());
        response.setCustomerName(conversation.getCustomer().getName());
        response.setCustomerEmail(conversation.getCustomer().getEmail());
        response.setCustomerAvatar(conversation.getCustomer().getAvatarUrl());
        
        if (conversation.getAdmin() != null) {
            response.setAdminId(conversation.getAdmin().getId());
            response.setAdminName(conversation.getAdmin().getName());
        }
        
        response.setLastMessageContent(conversation.getLastMessageContent());
        response.setLastMessageTimestamp(conversation.getLastMessageTimestamp());
        response.setUnreadCountCustomer(conversation.getUnreadCountCustomer());
        response.setUnreadCountAdmin(conversation.getUnreadCountAdmin());
        response.setStatus(conversation.getStatus().toString());
        response.setCreatedAt(conversation.getCreatedAt());
        response.setUpdatedAt(conversation.getUpdatedAt());
        response.setIsCustomerOnline(true); // This would be determined by WebSocket connection status
        
        if (includeMessages) {
            List<Message> messages = messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
            response.setRecentMessages(messages.stream()
                    .map(this::convertToMessageResponse)
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
}
