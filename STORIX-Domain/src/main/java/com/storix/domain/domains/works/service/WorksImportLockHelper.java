package com.storix.domain.domains.works.service;

import com.storix.common.utils.RedisKeyStatic;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorksImportLockHelper {

    private static final String KEY = RedisKeyStatic.Works.IMPORT_LOCK;
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public Optional<String> tryLock() {
        String token = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(KEY, token, TTL);
        return Boolean.TRUE.equals(locked) ? Optional.of(token) : Optional.empty();
    }

    public void unlock(String token) {
        redisTemplate.execute(UNLOCK_SCRIPT, List.of(KEY), token);
    }
}
