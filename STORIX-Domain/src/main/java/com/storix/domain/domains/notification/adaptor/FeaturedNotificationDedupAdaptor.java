package com.storix.domain.domains.notification.adaptor;

import com.storix.common.utils.RedisKeyStatic;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class FeaturedNotificationDedupAdaptor {

    private static final Duration DEDUP_TTL = Duration.ofDays(2);
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final StringRedisTemplate redisTemplate;

    public boolean markFeedIfFirstToday(Long feedId) {
        return markIfFirstToday(RedisKeyStatic.Notification.FEATURED_FEED_PREFIX, feedId);
    }

    public boolean markTopicRoomIfFirstToday(Long topicRoomId) {
        return markIfFirstToday(RedisKeyStatic.Notification.FEATURED_TOPIC_ROOM_PREFIX, topicRoomId);
    }

    // 발행에 실패했을 때 다음 실행에서 다시 보낼 수 있게 표시를 지운다
    public void unmarkFeedToday(Long feedId) {
        redisTemplate.delete(todayKey(RedisKeyStatic.Notification.FEATURED_FEED_PREFIX, feedId));
    }

    public void unmarkTopicRoomToday(Long topicRoomId) {
        redisTemplate.delete(todayKey(RedisKeyStatic.Notification.FEATURED_TOPIC_ROOM_PREFIX, topicRoomId));
    }

    // SETNX + TTL 원자 연산 -> 오늘 첫 선정이면 true, 이미 보냈으면 false
    private boolean markIfFirstToday(String prefix, Long id) {
        Boolean first = redisTemplate.opsForValue().setIfAbsent(todayKey(prefix, id), "1", DEDUP_TTL);
        return Boolean.TRUE.equals(first);
    }

    private String todayKey(String prefix, Long id) {
        return prefix + LocalDate.now().format(DATE) + ":" + id;
    }
}
