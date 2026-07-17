package com.storix.domain.domains.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;


@Slf4j
@Component
@RequiredArgsConstructor
public class EventContentCacheHelper {

    private static final String EMPTY = "EMPTY";
    private static final Duration EMPTY_TTL = Duration.ofMinutes(10);
    private static final Duration MIN_TTL = Duration.ofSeconds(1);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> Optional<T> getActive(String key, Class<T> type,
                                     Supplier<Optional<T>> loader,
                                     Function<T, LocalDateTime> endAtExtractor) {
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            if (EMPTY.equals(cached)) {
                return Optional.empty();
            }
            try {
                return Optional.of(objectMapper.readValue(cached, type));
            } catch (Exception e) {
                redisTemplate.delete(key);
            }
        }

        Optional<T> result = loader.get();
        try {
            if (result.isPresent()) {
                T value = result.get();
                redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttlUntil(endAtExtractor.apply(value)));
            } else {
                redisTemplate.opsForValue().set(key, EMPTY, EMPTY_TTL);
            }
        } catch (Exception e) {
            log.warn(">>> [EventCache] cache write failed. key={}", key, e);
        }
        return result;
    }

    public <T> List<T> getActiveList(String key, Class<T> elementType,
                                     Supplier<List<T>> loader,
                                     Function<T, LocalDateTime> endAtExtractor) {
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            if (EMPTY.equals(cached)) {
                return List.of();
            }
            try {
                return objectMapper.readValue(cached,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
            } catch (Exception e) {
                redisTemplate.delete(key);
            }
        }

        List<T> result = loader.get();
        try {
            if (!result.isEmpty()) {
                // 하나라도 종료되면 목록이 바뀌므로 가장 이른 종료시각까지만 캐싱
                LocalDateTime earliestEnd = result.stream()
                        .map(endAtExtractor)
                        .min(Comparator.naturalOrder())
                        .orElseThrow();
                redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), ttlUntil(earliestEnd));
            } else {
                redisTemplate.opsForValue().set(key, EMPTY, EMPTY_TTL);
            }
        } catch (Exception e) {
            log.warn(">>> [EventCache] cache write failed. key={}", key, e);
        }
        return result;
    }

    public void evict(String key) {
        redisTemplate.delete(key);
    }

    private static Duration ttlUntil(LocalDateTime endAt) {
        Duration remaining = Duration.between(LocalDateTime.now(), endAt);
        return remaining.compareTo(MIN_TTL) < 0 ? MIN_TTL : remaining;
    }
}
