package com.storix.domain.domains.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.event.dto.OneTimeAppEventResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
public class UserAppEventCacheHelper {

    private static final Duration TTL = Duration.ofMinutes(10);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public UserAppEventCacheHelper(@Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate,
                                   ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<OneTimeAppEventResponse> getPendingEvents(Long userId, Supplier<List<OneTimeAppEventResponse>> loader) {
        String key = key(userId);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue((String) cached, new TypeReference<>() {});
            } catch (Exception e) {
                redisTemplate.delete(key);
            }
        }

        List<OneTimeAppEventResponse> result = loader.get();
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), TTL);
        } catch (Exception e) {
            log.warn(">>> [UserAppEvent] cache write failed. key={}", key, e);
        }
        return result;
    }

    public void evict(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return STORIXStatic.PENDING_APP_EVENTS_KEY_PREFIX + userId;
    }
}
