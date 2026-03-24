package com.im.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 配置消息代理前缀
        // /topic 用于广播消息（群组）
        // /queue 用于点对点消息（单聊）
        config.enableSimpleBroker("/topic", "/queue");
        
        // 配置应用目的地前缀（客户端发送消息时使用）
        config.setApplicationDestinationPrefixes("/app");
        
        // 配置用户目的地前缀（用于点对点消息）
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket连接端点，客户端通过此端点建立STOMP连接
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // 同时注册不使用SockJS的WebSocket端点
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
