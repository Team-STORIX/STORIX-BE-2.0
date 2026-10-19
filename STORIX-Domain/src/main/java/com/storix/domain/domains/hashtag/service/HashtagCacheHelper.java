package com.storix.domain.domains.hashtag.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.common.utils.RedisKeyStatic;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.domain.domains.works.domain.Genre;

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
    private final ObjectMapper objectMapper;

    private static final String TOTAL_WORKS_KEY = RedisKeyStatic.Hashtag.TOTAL_WORKS;
    private static final String DF_HASH_KEY = RedisKeyStatic.Hashtag.DF;
    private static final String POPULAR_HASH_KEY = RedisKeyStatic.Hashtag.POPULAR;
    private static final String ALL_GENRES = "ALL";
    private static final TypeReference<List<HashtagRecommendResponseDto>> POPULAR_TYPE = new TypeReference<>() {};
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

    // 장르 조합별 인기 해시태그 순위. 트랜잭션 밖에서 불러야 캐시가 맞을 때 DB 커넥션을 안 잡는다
    public List<HashtagRecommendResponseDto> getOrLoadPopular(Set<Genre> genres, Supplier<List<HashtagRecommendResponseDto>> loader) {
        String field = genres.isEmpty() ? ALL_GENRES : genres.stream().map(Genre::name).sorted().collect(Collectors.joining(","));
        Object cached = redisTemplate.opsForHash().get(POPULAR_HASH_KEY, field);
        if (cached != null) {
            try {
                return objectMapper.readValue((String) cached, POPULAR_TYPE);
            } catch (JsonProcessingException e) {
                log.warn(">>> [HashtagCache] 인기 해시태그 캐시 읽기 실패", e);
            }
        }
        List<HashtagRecommendResponseDto> loaded = loader.get();
        try {
            redisTemplate.opsForHash().put(POPULAR_HASH_KEY, field, objectMapper.writeValueAsString(loaded));
            redisTemplate.expire(POPULAR_HASH_KEY, META_TTL);
        } catch (JsonProcessingException e) {
            log.warn(">>> [HashtagCache] 인기 해시태그 캐시 쓰기 실패", e);
        }
        return loaded;
    }

    // 전체 메타 캐시 무효화
    public void evictGlobalMeta() {
        redisTemplate.delete(List.of(TOTAL_WORKS_KEY, DF_HASH_KEY, POPULAR_HASH_KEY));
    }
}