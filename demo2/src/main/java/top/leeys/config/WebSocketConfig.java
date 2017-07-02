package top.leeys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import top.leeys.controller.SocketHandler;
import top.leeys.interceptor.MyHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Bean
    public SocketHandler webSocketHandler() {
        return new SocketHandler();
    }
    
    @Bean
    public MyHandshakeInterceptor handshakeInterceptor() {
        return new MyHandshakeInterceptor();
    }
    @Override 
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(webSocketHandler(), "/socket").addInterceptors(handshakeInterceptor()); 
    }

}
