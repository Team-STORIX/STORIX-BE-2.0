package com.storix.infrastructure.external.topicroom;

import com.storix.common.utils.RedisKeyStatic;
import com.storix.domain.domains.topicroom.application.port.TopicRoomPushBatchPort;
import com.storix.domain.domains.topicroom.dto.PendingChatPush;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisTopicRoomPushBatchAdapter implements TopicRoomPushBatchPort {

    private static final Duration SEND_INTERVAL = Duration.ofSeconds(30);
    private static final Duration PENDING_TTL = Duration.ofMinutes(5);
    private static final Duration ANCHOR_TTL = Duration.ofHours(1);

    private static final String FIELD_MESSAGE_ID = "messageId";
    private static final String FIELD_SENDER_ID = "senderId";
    private static final String FIELD_NICKNAME = "nickname";
    private static final String FIELD_MESSAGE = "message";

    private static final RedisScript<List> DRAIN_SCRIPT = new DefaultRedisScript<>("""
            local pending = redis.call('HGETALL', KEYS[1])
            redis.call('DEL', KEYS[1])
            redis.call('ZREM', KEYS[2], ARGV[1])
            return pending
            """, List.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTopicRoomPushBatchAdapter(
            @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    // SETNX + TTL 이라 서버가 여러 대여도 한 곳만 발송을 가져간다
    @Override
    public boolean tryAcquireSendSlot(Long roomId) {
        try {
            return Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(gateKey(roomId), "1", SEND_INTERVAL));
        } catch (Exception e) {
            // Redis 장애 시 묶음을 포기하고 발송을 허용
            log.warn(">>>> [TopicRoomPush] 게이트 선점 실패 roomId={}, cause={}", roomId, e.getMessage());
            return true;
        }
    }

    @Override
    public void accumulate(Long roomId, Long messageId, Long senderId, String senderNickname, String message) {
        try {
            String pendingKey = pendingKey(roomId);
            redisTemplate.opsForHash().putAll(pendingKey, Map.of(
                    FIELD_MESSAGE_ID, String.valueOf(messageId),
                    FIELD_SENDER_ID, String.valueOf(senderId),
                    FIELD_NICKNAME, senderNickname,
                    FIELD_MESSAGE, message));
            redisTemplate.expire(pendingKey, PENDING_TTL);

            // 이미 예약돼 있으면 앞선 예약 시각을 유지
            redisTemplate.opsForZSet().addIfAbsent(
                    RedisKeyStatic.TopicRoom.PUSH_QUEUE,
                    String.valueOf(roomId),
                    dueAt(roomId));
        } catch (Exception e) {
            log.warn(">>>> [TopicRoomPush] 누적 실패 roomId={}, cause={}", roomId, e.getMessage());
        }
    }

    @Override
    public boolean enqueue(Long roomId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForZSet().addIfAbsent(
                    RedisKeyStatic.TopicRoom.PUSH_QUEUE, String.valueOf(roomId), dueAt(roomId)));
        } catch (Exception e) {
            log.warn(">>>> [TopicRoomPush] 대기열 등록 실패 roomId={}, cause={}", roomId, e.getMessage());
            return false;
        }
    }

    @Override
    public List<Long> pollDueRoomIds(int limit) {
        try {
            Set<Object> due = redisTemplate.opsForZSet().rangeByScore(
                    RedisKeyStatic.TopicRoom.PUSH_QUEUE, 0, System.currentTimeMillis(), 0, limit);
            if (due == null || due.isEmpty()) {
                return List.of();
            }
            return due.stream().map(String::valueOf).map(Long::valueOf).toList();
        } catch (Exception e) {
            log.warn(">>>> [TopicRoomPush] 대기열 조회 실패 cause={}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public PendingChatPush drain(Long roomId) {
        try {
            List<?> flat = redisTemplate.execute(
                    DRAIN_SCRIPT,
                    List.of(pendingKey(roomId), RedisKeyStatic.TopicRoom.PUSH_QUEUE),
                    String.valueOf(roomId));

            if (flat == null || flat.isEmpty()) {
                return null;
            }

            Map<String, String> pending = new HashMap<>();
            for (int i = 0; i + 1 < flat.size(); i += 2) {
                pending.put(String.valueOf(flat.get(i)), String.valueOf(flat.get(i + 1)));
            }
            return new PendingChatPush(
                    Long.valueOf(pending.get(FIELD_MESSAGE_ID)),
                    Long.valueOf(pending.get(FIELD_SENDER_ID)),
                    pending.get(FIELD_NICKNAME),
                    pending.get(FIELD_MESSAGE));
        } catch (Exception e) {
            log.warn(">>>> [TopicRoomPush] 누적분 조회 실패 roomId={}, cause={}", roomId, e.getMessage());
            return null;
        }
    }

    @Override
    public Long findLastPushedMessageId(Long roomId) {
        try {
            Object anchor = redisTemplate.opsForValue().get(anchorKey(roomId));
            return anchor == null ? null : Long.valueOf(String.valueOf(anchor));
        } catch (Exception e) {
            log.warn(">>>> [TopicRoomPush] 앵커 조회 실패 roomId={}, cause={}", roomId, e.getMessage());
            return null;
        }
    }

    @Override
    public void markPushed(Long roomId, Long messageId) {
        try {
            redisTemplate.opsForValue().set(anchorKey(roomId), String.valueOf(messageId), ANCHOR_TTL);
        } catch (Exception e) {
            log.warn(">>>> [TopicRoomPush] 앵커 갱신 실패 roomId={}, cause={}", roomId, e.getMessage());
        }
    }

    private long dueAt(Long roomId) {
        Long remaining = redisTemplate.getExpire(gateKey(roomId), TimeUnit.MILLISECONDS);
        long wait = (remaining == null || remaining < 0) ? 0 : remaining;
        return System.currentTimeMillis() + wait;
    }

    private String gateKey(Long roomId) {
        return RedisKeyStatic.TopicRoom.PUSH_GATE_PREFIX + roomId;
    }

    private String pendingKey(Long roomId) {
        return RedisKeyStatic.TopicRoom.PUSH_PENDING_PREFIX + roomId;
    }

    private String anchorKey(Long roomId) {
        return RedisKeyStatic.TopicRoom.PUSH_ANCHOR_PREFIX + roomId;
    }
}
