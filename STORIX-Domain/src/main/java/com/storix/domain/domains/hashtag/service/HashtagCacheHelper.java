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

    // 없으면 null
    public Long findTotalWorksCount() {
        String cached = redisTemplate.opsForValue().get(TOTAL_WORKS_KEY);
        return cached != null ? Long.parseLong(cached) : null;
    }

    public void saveTotalWorksCount(long count) {
        redisTemplate.opsForValue().set(TOTAL_WORKS_KEY, String.valueOf(count), META_TTL);
    }

    // 캐시에 있는 것만 HMGET 한 번으로 읽는다
    public Map<Long, Long> findDocumentFrequencies(Set<Long> hashtagIds) {
        List<Long> idList = new ArrayList<>(hashtagIds);
        List<String> fields = idList.stream().map(String::valueOf).toList();

        // HMGET: 순서가 fields와 동일하게 반환되며 캐시 미스는 null
        List<String> cachedValues = redisTemplate.<String, String>opsForHash().multiGet(DF_HASH_KEY, fields);

        Map<Long, Long> result = new HashMap<>();
        for (int i = 0; i < idList.size(); i++) {
            String val = cachedValues.get(i);
            if (val != null) {
                result.put(idList.get(i), Long.parseLong(val));
            }
        }
        return result;
    }

    // 기존 값은 두고 받은 항목만 더한다
    public void saveDocumentFrequencies(Map<Long, Long> frequencies) {
        if (frequencies.isEmpty()) {
            return;
        }
        Map<String, String> toStore = frequencies.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue())
                ));
        redisTemplate.<String, String>opsForHash().putAll(DF_HASH_KEY, toStore);
        redisTemplate.expire(DF_HASH_KEY, META_TTL);
    }

    // 장르 조합별 인기 해시태그 순위. 없거나 못 읽으면 null
    public List<HashtagRecommendResponseDto> findPopular(Set<Genre> genres) {
        String cached = redisTemplate.<String, String>opsForHash().get(POPULAR_HASH_KEY, popularField(genres));
        if (cached == null) {
            return null;
        }
        try {
            return objectMapper.readValue(cached, POPULAR_TYPE);
        } catch (JsonProcessingException e) {
            log.warn(">>> [HashtagCache] 인기 해시태그 캐시 읽기 실패", e);
            return null;
        }
    }

    public void savePopular(Set<Genre> genres, List<HashtagRecommendResponseDto> ranked) {
        try {
            redisTemplate.<String, String>opsForHash().put(POPULAR_HASH_KEY, popularField(genres), objectMapper.writeValueAsString(ranked));
            redisTemplate.expire(POPULAR_HASH_KEY, META_TTL);
        } catch (JsonProcessingException e) {
            log.warn(">>> [HashtagCache] 인기 해시태그 캐시 쓰기 실패", e);
        }
    }

    private String popularField(Set<Genre> genres) {
        return genres.isEmpty() ? ALL_GENRES : genres.stream().map(Genre::name).sorted().collect(Collectors.joining(","));
    }

    // 전체 메타 캐시 무효화
    public void evictGlobalMeta() {
        redisTemplate.delete(List.of(TOTAL_WORKS_KEY, DF_HASH_KEY, POPULAR_HASH_KEY));
    }
}