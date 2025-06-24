package com.michaelcao.bookstore_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Đăng ký interceptor xác thực cho tin nhắn đến
        registration.interceptors(webSocketAuthInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry the messages
        // back to the client on destinations prefixed with "/topic"
        config.enableSimpleBroker("/topic");
        
        // Định dạng "/app" cho tin nhắn được gắn vào
        // @MessageMapping-annotated methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint "/ws", bật tùy chọn SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000") 
                .withSockJS();
    }
}
