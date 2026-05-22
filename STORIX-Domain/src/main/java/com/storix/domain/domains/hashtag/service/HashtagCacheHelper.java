package com.storix.domain.domains.hashtag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashtagCacheHelper {

    private final StringRedisTemplate redisTemplate;

    private static final String TOTAL_WORKS_KEY = "hashtag:meta:total_works";
    private static final String DF_HASH_KEY = "hashtag:df";
    private static final Duration META_TTL = Duration.ofDays(7);

    public long getOrLoadTotalWorksCount(Supplier<Long> loader) {
        String cached = redisTemplate.opsForValue().get(TOTAL_WORKS_KEY);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        long count = loader.get();
        redisTemplate.opsForValue().set(TOTAL_WORKS_KEY, String.valueOf(count), META_TTL);
        return count;
    }

    // 요청된 hashtagId 중 캐시 미스만 DB 조회 후 Hash에 추가 저장
    // HMGET으로 한 번의 Redis 호출로 여러 field를 읽어 N+1 쿼리를 방지
    public Map<Long, Long> getOrLoadDocumentFrequencies(Set<Long> hashtagIds, Function<Set<Long>, Map<Long, Long>> loader) {
        List<Long> idList = new ArrayList<>(hashtagIds);
        List<String> fields = idList.stream().map(String::valueOf).toList();

        // HMGET: 순서가 fields와 동일하게 반환되며 캐시 미스는 null
        List<String> cachedValues = redisTemplate.<String, String>opsForHash().multiGet(DF_HASH_KEY, fields);

        Map<Long, Long> result = new HashMap<>();
        Set<Long> missingIds = new HashSet<>();

        for (int i = 0; i < idList.size(); i++) {
            String val = cachedValues.get(i);
            if (val != null) {
                result.put(idList.get(i), Long.parseLong(val));
            } else {
                missingIds.add(idList.get(i));
            }
        }

        if (!missingIds.isEmpty()) {
            Map<Long, Long> fromDb = loader.apply(missingIds);
            result.putAll(fromDb);

            // 미스된 항목만 Hash에 추가 (기존 캐시 엔트리는 덮어쓰지 않음)
            Map<String, String> toStore = fromDb.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> String.valueOf(e.getKey()),
                            e -> String.valueOf(e.getValue())
                    ));
            if (!toStore.isEmpty()) {
                redisTemplate.opsForHash().putAll(DF_HASH_KEY, toStore);
                redisTemplate.expire(DF_HASH_KEY, META_TTL);
            }
        }

        return result;
    }

    // 전체 메타 캐시 무효화
    public void evictGlobalMeta() {
        redisTemplate.delete(List.of(TOTAL_WORKS_KEY, DF_HASH_KEY));
    }
}