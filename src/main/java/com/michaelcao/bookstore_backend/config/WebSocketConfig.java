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
        // Register the authentication interceptor for inbound messages
        registration.interceptors(webSocketAuthInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry the messages
        // back to the client on destinations prefixed with "/topic"
        config.enableSimpleBroker("/topic");
        
        // Designate the "/app" prefix for messages that are bound for
        // @MessageMapping-annotated methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling SockJS fallback options
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000") // Frontend URL
                .withSockJS();
    }
}
