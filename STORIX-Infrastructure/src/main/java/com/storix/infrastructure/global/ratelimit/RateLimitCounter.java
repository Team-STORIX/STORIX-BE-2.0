package com.storix.infrastructure.global.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitCounter {

    private static final RedisScript<Long> INCREMENT = new DefaultRedisScript<>(
            "local count = redis.call('INCR', KEYS[1]) "
                    + "if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end "
                    + "return count",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    // Redis 장애 시 요청 전체가 막히지 않도록 0 으로 통과
    public long increment(String key, long ttlSeconds) {
        try {
            Long count = redisTemplate.execute(INCREMENT, List.of(key), String.valueOf(ttlSeconds));
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.warn(">>> [RateLimit] 횟수 집계 실패 key={}", key, e);
            return 0;
        }
    }
}
