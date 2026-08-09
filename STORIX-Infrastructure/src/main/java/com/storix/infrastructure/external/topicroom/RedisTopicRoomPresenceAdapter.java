package com.storix.infrastructure.external.topicroom;

import com.storix.common.utils.RedisKeyStatic;
import com.storix.domain.domains.topicroom.application.port.TopicRoomPresencePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RedisTopicRoomPresenceAdapter implements TopicRoomPresencePort {

    // STOMP heartbeat 가 10초라 여러 번 놓쳐도 견디도록 잡은 값
    private static final Duration STALE_AFTER = Duration.ofSeconds(60);
    private static final Duration TTL = Duration.ofHours(12);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTopicRoomPresenceAdapter(
            @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void enter(Long roomId, Long userId, String sessionId) {
        try {
            String key = key(roomId);
            redisTemplate.opsForZSet().add(key, member(userId, sessionId), System.currentTimeMillis());
            redisTemplate.expire(key, TTL);
        } catch (Exception e) {
            log.warn(">>>> [TopicRoomPresence] 접속 기록 실패 roomId={}, userId={}, cause={}", roomId, userId, e.getMessage());
        }
    }

    @Override
    public void leave(Long roomId, Long userId, String sessionId) {
        try {
            redisTemplate.opsForZSet().remove(key(roomId), member(userId, sessionId));
        } catch (Exception e) {
            log.warn(">>>> [TopicRoomPresence] 접속 해제 실패 roomId={}, userId={}, cause={}", roomId, userId, e.getMessage());
        }
    }

    // 인스턴스가 죽으면 DISCONNECT 를 못 받으므로 갱신이 끊긴 멤버는 접속자에서 제외
    @Override
    public Set<Long> findOnlineUserIds(Long roomId) {
        try {
            String key = key(roomId);
            long threshold = System.currentTimeMillis() - STALE_AFTER.toMillis();
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, threshold);

            Set<Object> members = redisTemplate.opsForZSet().rangeByScore(key, threshold, Double.MAX_VALUE);
            if (members == null || members.isEmpty()) {
                return Collections.emptySet();
            }
            return members.stream()
                    .map(String::valueOf)
                    .map(this::parseUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            // 조회 실패 시 아무도 접속 안 한 것으로 간주해 푸시를 보내는 쪽으로
            log.warn(">>>> [TopicRoomPresence] 접속자 조회 실패 roomId={}, cause={}", roomId, e.getMessage());
            return Collections.emptySet();
        }
    }

    private Long parseUserId(String member) {
        int separator = member.indexOf(':');
        if (separator <= 0) return null;
        try {
            return Long.parseLong(member.substring(0, separator));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String key(Long roomId) {
        return RedisKeyStatic.TopicRoom.ONLINE_PREFIX + roomId;
    }

    private String member(Long userId, String sessionId) {
        return userId + ":" + sessionId;
    }
}
