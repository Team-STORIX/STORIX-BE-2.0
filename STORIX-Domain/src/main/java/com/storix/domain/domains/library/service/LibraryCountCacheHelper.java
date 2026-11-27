package com.storix.domain.domains.library.service;

import com.storix.common.utils.RedisKeyStatic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryCountCacheHelper {

    private static final Duration TTL = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;

    // 없거나 Redis 가 실패하면 null. 호출한 쪽이 DB 에서 센다
    public Integer findReviewCount(Long userId) {
        try {
            String cached = redisTemplate.opsForValue().get(reviewCountKey(userId));
            return cached == null ? null : Integer.valueOf(cached);
        } catch (Exception e) {
            log.atWarn()
                    .addKeyValue("userId", userId)
                    .addKeyValue("cause", e.getMessage())
                    .log(">>> [Library] 리뷰 수 캐시 조회 실패");
            return null;
        }
    }

    public void saveReviewCount(Long userId, int count) {
        try {
            redisTemplate.opsForValue().set(reviewCountKey(userId), String.valueOf(count), TTL);
        } catch (Exception e) {
            log.atWarn()
                    .addKeyValue("userId", userId)
                    .addKeyValue("cause", e.getMessage())
                    .log(">>> [Library] 리뷰 수 캐시 저장 실패");
        }
    }

    public void evictReviewCount(Long userId) {
        try {
            redisTemplate.delete(reviewCountKey(userId));
        } catch (Exception e) {
            log.atWarn()
                    .addKeyValue("userId", userId)
                    .addKeyValue("cause", e.getMessage())
                    .log(">>> [Library] 리뷰 수 캐시 삭제 실패");
        }
    }

    private String reviewCountKey(Long userId) {
        return RedisKeyStatic.Library.REVIEW_COUNT_PREFIX + userId;
    }
}
