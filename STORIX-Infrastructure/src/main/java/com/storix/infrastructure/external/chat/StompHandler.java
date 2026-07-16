package com.storix.infrastructure.external.chat;

import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.Role;
import com.storix.infrastructure.external.topicroom.RedisTopicRoomActiveUserNumberAdapter;
import com.storix.infrastructure.external.topicroom.TopicRoomActiveUserNumberRedisSubscriber;
import com.storix.infrastructure.global.TokenProvider;
import com.storix.infrastructure.global.dto.AccessTokenInfo;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {
    private final TokenProvider tokenProvider;
    private final RedisMessageListenerContainer container;
    private final RedisSubscriber subscriber;
    private final TopicRoomActiveUserNumberRedisSubscriber activeUserNumberSubscriber;

    private final Map<String, ChannelTopic> chatTopics = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> chatRoomSubscriberCounts = new ConcurrentHashMap<>();
    private final Map<String, ChannelTopic> activeUserNumberTopics = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> activeUserNumberSubscriberCounts = new ConcurrentHashMap<>();
    private final Map<String, Map<String, SubscriptionTarget>> sessionSubscriptionMap = new ConcurrentHashMap<>();

    public StompHandler(
            TokenProvider tp,
            RedisMessageListenerContainer c,
            @Lazy RedisSubscriber s,
            @Lazy TopicRoomActiveUserNumberRedisSubscriber activeUserNumberSubscriber
    ) {
        this.tokenProvider = tp;
        this.container = c;
        this.subscriber = s;
        this.activeUserNumberSubscriber = activeUserNumberSubscriber;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            log.warn(">>>> [STOMP_DIAG] inbound message without StompHeaderAccessor headers={}", message.getHeaders().keySet());
            return message;
        }

        StompCommand command = accessor.getCommand();
        log.info(">>>> [STOMP_DIAG] inbound command={}, sessionId={}, destination={}, subscriptionId={}, nativeHeaderKeys={}, user={}",
                command,
                accessor.getSessionId(),
                accessor.getDestination(),
                accessor.getSubscriptionId(),
                accessor.toNativeHeaderMap().keySet(),
                accessor.getUser() != null ? accessor.getUser().getName() : null);

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

                log.debug(">>>> [STOMP] 인증 성공: UserID {}", info.userId());
            } catch (Exception e) {
                log.warn(">>>> [STOMP] 인증 실패: sessionId={}, exceptionType={}, message={}",
                        accessor.getSessionId(),
                        e.getClass().getSimpleName(),
                        e.getMessage());
                throw new MessageDeliveryException("UNAUTHORIZED");
            }
        } else {
            log.warn(">>>> [STOMP] 인증 실패: sessionId={}, reason=NO_TOKEN_OR_INVALID_BEARER", accessor.getSessionId());
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
                    .put(subId, SubscriptionTarget.chat(roomId));

            // 카운트 증가 및 리스너 등록
            increaseChatCounter(roomId);
        } else if (destination != null
                && destination.startsWith("/sub/topic-rooms/")
                && destination.endsWith("/active-users")) {
            String roomId = extractActiveUserNumberRoomId(destination);
            String sessionId = accessor.getSessionId();
            String subId = accessor.getSubscriptionId();

            if (roomId == null || subId == null) return;

            sessionSubscriptionMap
                    .computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
                    .put(subId, SubscriptionTarget.activeUsers(roomId));

            increaseActiveUserNumberCounter(roomId);
        }
    }

    private void handleUnsubscribe(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String subId = accessor.getSubscriptionId();

        Map<String, SubscriptionTarget> subscriptions = sessionSubscriptionMap.get(sessionId);
        if (subscriptions != null && subId != null) {
            SubscriptionTarget target = subscriptions.remove(subId);
            if (target != null) {
                decreaseCounter(target);
                log.info(">>>> [STOMP] 구독 해제 완료: Session={}, Type={}, Room={}",
                        sessionId, target.type(), target.roomId());
            }
        }
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Map<String, SubscriptionTarget> subscriptions = sessionSubscriptionMap.remove(sessionId);

        if (subscriptions != null) {
            log.info(">>>> [STOMP] 연결 종료 - 전체 구독 정리 시작: Session={}", sessionId);
            for (SubscriptionTarget target : subscriptions.values()) {
                decreaseCounter(target);
            }
        }
    }

    private void increaseChatCounter(String roomId) {
        AtomicInteger counter = chatRoomSubscriberCounts.computeIfAbsent(roomId, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();

        if (count == 1) {
            chatTopics.computeIfAbsent(roomId, id -> {
                ChannelTopic topic = new ChannelTopic("room:" + id);
                container.addMessageListener(subscriber, topic);
                log.info(">>>> [Redis] 첫 번째 구독자 발생 - 리스너 등록됨: room:{}", id);
                return topic;
            });
        }
        log.debug(">>>> [Redis] 방 참여자 현황: room:{} ({}명)", roomId, count);
    }

    private void decreaseChatCounter(String roomId) {
        AtomicInteger counter = chatRoomSubscriberCounts.get(roomId);
        if (counter != null) {
            int remaining = counter.decrementAndGet();
            if (remaining <= 0) {
                ChannelTopic topic = chatTopics.remove(roomId);
                if (topic != null) {
                    container.removeMessageListener(subscriber, topic);
                    log.info(">>>> [Redis] 마지막 구독자 퇴장 - 리스너 해제됨: room:{}", roomId);
                }
                chatRoomSubscriberCounts.remove(roomId);
            }
            log.debug(">>>> [Redis] 방 참여자 현황: room:{} ({}명)", roomId, Math.max(0, remaining));
        }
    }

    private void increaseActiveUserNumberCounter(String roomId) {
        AtomicInteger counter = activeUserNumberSubscriberCounts.computeIfAbsent(roomId, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();

        if (count == 1) {
            activeUserNumberTopics.computeIfAbsent(roomId, id -> {
                ChannelTopic topic = new ChannelTopic(RedisTopicRoomActiveUserNumberAdapter.getChannel(id));
                container.addMessageListener(activeUserNumberSubscriber, topic);
                log.info(">>>> [Redis] 첫 번째 활성 유저 수 구독자 발생 - 리스너 등록됨: {}", topic.getTopic());
                return topic;
            });
        }
        log.debug(">>>> [Redis] 활성 유저 수 구독 현황: room:{} ({}명)", roomId, count);
    }

    private void decreaseActiveUserNumberCounter(String roomId) {
        AtomicInteger counter = activeUserNumberSubscriberCounts.get(roomId);
        if (counter != null) {
            int remaining = counter.decrementAndGet();
            if (remaining <= 0) {
                ChannelTopic topic = activeUserNumberTopics.remove(roomId);
                if (topic != null) {
                    container.removeMessageListener(activeUserNumberSubscriber, topic);
                    log.info(">>>> [Redis] 마지막 활성 유저 수 구독자 퇴장 - 리스너 해제됨: {}", topic.getTopic());
                }
                activeUserNumberSubscriberCounts.remove(roomId);
            }
            log.debug(">>>> [Redis] 활성 유저 수 구독 현황: room:{} ({}명)", roomId, Math.max(0, remaining));
        }
    }

    private void decreaseCounter(SubscriptionTarget target) {
        if (SubscriptionType.CHAT.equals(target.type())) {
            decreaseChatCounter(target.roomId());
            return;
        }

        decreaseActiveUserNumberCounter(target.roomId());
    }

    private String extractActiveUserNumberRoomId(String destination) {
        String prefix = "/sub/topic-rooms/";
        String suffix = "/active-users";
        if (destination.length() <= prefix.length() + suffix.length()) {
            return null;
        }

        return destination.substring(prefix.length(), destination.length() - suffix.length());
    }

    @PreDestroy
    public void cleanup() {
        log.info(">>>> [Cleanup] STOMP Redis 리스너 캐시 정리 시작");
        chatTopics.clear();
        chatRoomSubscriberCounts.clear();
        activeUserNumberTopics.clear();
        activeUserNumberSubscriberCounts.clear();
        sessionSubscriptionMap.clear();
    }

    private enum SubscriptionType {
        CHAT,
        ACTIVE_USERS
    }

    private record SubscriptionTarget(SubscriptionType type, String roomId) {
        private static SubscriptionTarget chat(String roomId) {
            return new SubscriptionTarget(SubscriptionType.CHAT, roomId);
        }

        private static SubscriptionTarget activeUsers(String roomId) {
            return new SubscriptionTarget(SubscriptionType.ACTIVE_USERS, roomId);
        }
    }
}
