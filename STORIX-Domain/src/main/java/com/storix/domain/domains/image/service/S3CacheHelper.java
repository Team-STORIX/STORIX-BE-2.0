package com.storix.domain.domains.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3CacheHelper {

    private final StringRedisTemplate redisTemplate;

    private static final String BOARD_KEY_PREFIX = "image:public:board:";
    private static final String PROFILE_KEY_PREFIX = "image:public:profile:";

    private static final long IMAGE_KEY_TTL_MINUTE = 20;

    private String keyFor(Long userId, String domainPrefix) {
        return domainPrefix + userId;
    }

    // Redis 캐싱
    public void cacheBoardKeys(Long userId, List<String> objectKeys) {
        cacheAsSet(keyFor(userId, BOARD_KEY_PREFIX), objectKeys);
    }

    public void cacheProfileKey(Long userId, String objectKey) {
        cacheAsSet(keyFor(userId, PROFILE_KEY_PREFIX), List.of(objectKey));
    }

    private static final DefaultRedisScript<Long> CACHE_SET_WITH_TTL_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    redis.call('SADD', KEYS[1], unpack(ARGV, 2))
                    redis.call('EXPIRE', KEYS[1], ARGV[1])
                    return 1
                    """,
                    Long.class
            );

    private void cacheAsSet(String redisKey, List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) return;

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(TimeUnit.MINUTES.toSeconds(IMAGE_KEY_TTL_MINUTE)));
        args.addAll(objectKeys);

        redisTemplate.execute(
                CACHE_SET_WITH_TTL_SCRIPT,
                List.of(redisKey),
                args.toArray()
        );
    }

    // 사용(소비)된 objectKey 를 유효 키 캐시에서 제거 — 이후 삭제된 S3 오브젝트로 재변경/재사용되는 것을 방지.
    // evict 는 커밋 이후 호출되므로 Redis 장애가 성공한 요청을 실패로 만들지 않게 예외를 삼킨다 (미소거 키는 TTL 로 만료).
    public void evictProfileKey(Long userId, String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        evictQuietly(keyFor(userId, PROFILE_KEY_PREFIX), List.of(objectKey));
    }

    public void evictBoardKeys(Long userId, List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) return;
        evictQuietly(keyFor(userId, BOARD_KEY_PREFIX), objectKeys);
    }

    private void evictQuietly(String redisKey, List<String> objectKeys) {
        try {
            redisTemplate.opsForSet().remove(redisKey, objectKeys.toArray());
        } catch (Exception e) {
            log.warn(">>> [S3CacheHelper] 소비된 objectKey 캐시 제거 실패 — TTL 만료까지 재사용 가능 상태 (redisKey={}, keys={})",
                    redisKey, objectKeys, e);
        }
    }

    // ObjectKeys 검증 로직
    public boolean isValidBoardKeys(Long userId, List<String> objectKeys) {
        return isValidAll(keyFor(userId, BOARD_KEY_PREFIX), objectKeys);
    }

    public boolean isValidProfileKey(Long userId, String objectKey) {
        return isValidAll(keyFor(userId, PROFILE_KEY_PREFIX), List.of(objectKey));
    }

    private static final DefaultRedisScript<Long> CONTAINS_ALL_REDIS_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    for i = 1, #ARGV do
                      if redis.call('SISMEMBER', KEYS[1], ARGV[i]) == 0 then
                        return 0
                      end
                    end
                    return 1
                    """,
                    Long.class
            );

    private boolean isValidAll(String redisKey, List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) return false;

        Long result = redisTemplate.execute(
                CONTAINS_ALL_REDIS_SCRIPT,
                Collections.singletonList(redisKey),
                (Object[]) objectKeys.toArray(new String[0])
        );

        return Long.valueOf(1L).equals(result);
    }

}