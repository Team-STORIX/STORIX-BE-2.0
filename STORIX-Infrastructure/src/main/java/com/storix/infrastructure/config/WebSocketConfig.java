package com.storix.infrastructure.config;

import com.storix.infrastructure.external.chat.StompHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp").setAllowedOriginPatterns(
                "https://storix.kr",
                "https://www.storix.kr",
                "https://api.storix.kr",
                "http://localhost:3000",
                "http://localhost:5173",
                "capacitor://localhost",
                "https://localhost",
                "https://storix-fe-git-develop-kim-yunseongs-projects.vercel.app",
                "https://storix-fe-git-main-kim-yunseongs-projects.vercel.app"
        ).addInterceptors(new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(
                    ServerHttpRequest request,
                    ServerHttpResponse response,
                    WebSocketHandler wsHandler,
                    Map<String, Object> attributes
            ) {
                log.info(">>>> [WS_HANDSHAKE] before uri={}, origin={}, remoteAddress={}, userAgent={}",
                        request.getURI(),
                        request.getHeaders().getOrigin(),
                        request.getRemoteAddress(),
                        request.getHeaders().getFirst("User-Agent"));
                return true;
            }

            @Override
            public void afterHandshake(
                    ServerHttpRequest request,
                    ServerHttpResponse response,
                    WebSocketHandler wsHandler,
                    Exception exception
            ) {
                if (exception == null) {
                    log.info(">>>> [WS_HANDSHAKE] after success uri={}, origin={}",
                            request.getURI(),
                            request.getHeaders().getOrigin());
                    return;
                }

                log.warn(">>>> [WS_HANDSHAKE] after failure uri={}, origin={}, exceptionType={}, message={}",
                        request.getURI(),
                        request.getHeaders().getOrigin(),
                        exception.getClass().getSimpleName(),
                        exception.getMessage(),
                        exception);
            }
        });
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
