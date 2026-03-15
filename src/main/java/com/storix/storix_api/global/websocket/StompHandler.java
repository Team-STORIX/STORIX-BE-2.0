package com.storix.storix_api.global.websocket;

import com.storix.storix_api.domains.chat.service.RedisSubscriber;
import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.domains.user.domain.Role;
import com.storix.storix_api.global.security.TokenProvider;
import com.storix.storix_api.global.security.dto.AccessTokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {
    private final TokenProvider tokenProvider;
    private final RedisMessageListenerContainer container;
    private final RedisSubscriber subscriber;

    private final Map<String, ChannelTopic> topics = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> roomSubscriberCounts = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> sessionSubscriptionMap = new ConcurrentHashMap<>();

    public StompHandler(TokenProvider tp, RedisMessageListenerContainer c, @Lazy RedisSubscriber s) {
        this.tokenProvider = tp;
        this.container = c;
        this.subscriber = s;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            handleSubscribe(accessor);
        } else if (StompCommand.UNSUBSCRIBE.equals(command)) {
            handleUnsubscribe(accessor);
        } else if (StompCommand.DISCONNECT.equals(command)) {
            handleDisconnect(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                AccessTokenInfo info = tokenProvider.parseAccessToken(token.substring(7));
                AuthUserDetails user = new AuthUserDetails(info.userId(), Role.fromValue(info.role().replace("ROLE_", "")));
                Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                accessor.setUser(auth);

                log.info(">>>> [STOMP] 인증 성공: UserID {}", info.userId());
            } catch (Exception e) {

                log.error(">>>> [STOMP] 인증 실패: {}", e.getMessage());
                throw new MessageDeliveryException("UNAUTHORIZED");
            }
        } else {
            throw new MessageDeliveryException("NO_TOKEN");
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination != null && destination.startsWith("/sub/chat/room/")) {
            String roomId = destination.substring(destination.lastIndexOf('/') + 1);
            String sessionId = accessor.getSessionId();
            String subId = accessor.getSubscriptionId();

            if (subId == null) return;

            // 세션 구독 정보 저장
            sessionSubscriptionMap
                    .computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
                    .put(subId, roomId);

            // 카운트 증가 및 리스너 등록
            increaseCounter(roomId);
        }
    }

    private void handleUnsubscribe(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String subId = accessor.getSubscriptionId();

        Map<String, String> subscriptions = sessionSubscriptionMap.get(sessionId);
        if (subscriptions != null && subId != null) {
            String roomId = subscriptions.remove(subId);
            if (roomId != null) {
                decreaseCounter(roomId);
                log.info(">>>> [STOMP] 구독 해제 완료: Session={}, Room={}", sessionId, roomId);
            }
        }
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Map<String, String> subscriptions = sessionSubscriptionMap.remove(sessionId);

        if (subscriptions != null) {
            log.info(">>>> [STOMP] 연결 종료 - 전체 구독 정리 시작: Session={}", sessionId);
            for (String roomId : subscriptions.values()) {
                decreaseCounter(roomId);
            }
        }
    }

    private void increaseCounter(String roomId) {
        AtomicInteger counter = roomSubscriberCounts.computeIfAbsent(roomId, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();

        if (count == 1) {
            topics.computeIfAbsent(roomId, id -> {
                ChannelTopic topic = new ChannelTopic("room:" + id);
                container.addMessageListener(subscriber, topic);
                log.info(">>>> [Redis] 첫 번째 구독자 발생 - 리스너 등록됨: room:{}", id);
                return topic;
            });
        }
        log.debug(">>>> [Redis] 방 참여자 현황: room:{} ({}명)", roomId, count);
    }

    private void decreaseCounter(String roomId) {
        AtomicInteger counter = roomSubscriberCounts.get(roomId);
        if (counter != null) {
            int remaining = counter.decrementAndGet();
            if (remaining <= 0) {
                ChannelTopic topic = topics.remove(roomId);
                if (topic != null) {
                    container.removeMessageListener(subscriber, topic);
                    log.info(">>>> [Redis] 마지막 구독자 퇴장 - 리스너 해제됨: room:{}", roomId);
                }
                roomSubscriberCounts.remove(roomId);
            }
            log.debug(">>>> [Redis] 방 참여자 현황: room:{} ({}명)", roomId, Math.max(0, remaining));
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info(">>>> [Cleanup] 채팅 리소스 캐시 정리 시작");
        topics.clear();
        roomSubscriberCounts.clear();
        sessionSubscriptionMap.clear();
    }
}