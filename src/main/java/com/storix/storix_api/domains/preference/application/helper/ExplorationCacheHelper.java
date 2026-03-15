package com.storix.storix_api.domains.preference.application.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.storix_api.domains.preference.dto.GenreScoreInfo;
import com.storix.storix_api.domains.preference.dto.PendingSwipeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExplorationCacheHelper {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHART_KEY_PREFIX = "exploration::chart::total::";
    private static final String DONE_KEY_PREFIX = "exploration::done::today::";
    private static final String PENDING_LIST_PREFIX = "exploration::pending::detail::";
    private static final String GLOBAL_QUEUE_KEY = "exploration::queue";
    private static final String DAILY_COUNT_KEY_PREFIX = "exploration::count::today::";
    private static final int MAX_QUEUE_SIZE = 10000;

    private static final String SUBMIT_SCRIPT =
            "if redis.call('exists', KEYS[1]) == 1 then return -1 end " +
                    "local q_len = redis.call('llen', KEYS[4]) " +
                    "if q_len > tonumber(ARGV[4]) then return -4 end " +
                    "local count = redis.call('incr', KEYS[2]) " +
                    "if count > 15 then return -2 end " +
                    "redis.call('rpush', KEYS[3], ARGV[1]) " +
                    "redis.call('rpush', KEYS[4], ARGV[1]) " +
                    "if count == 1 then redis.call('expire', KEYS[2], ARGV[2]) end " +
                    "redis.call('expire', KEYS[3], ARGV[3]) " +
                    "return count";

    public Long submitWithLua(Long userId, PendingSwipeDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            String doneKey = DONE_KEY_PREFIX + userId;
            String countKey = DAILY_COUNT_KEY_PREFIX + userId;
            String detailKey = PENDING_LIST_PREFIX + userId;

            long secondsToMidnight = java.time.Duration.between(LocalDateTime.now(),
                    LocalDateTime.now().toLocalDate().atTime(java.time.LocalTime.MAX)).getSeconds();

            return redisTemplate.execute(
                    new DefaultRedisScript<>(SUBMIT_SCRIPT, Long.class),
                    List.of(doneKey, countKey, detailKey, GLOBAL_QUEUE_KEY),
                    json,
                    "43200",
                    "43200",
                    String.valueOf(MAX_QUEUE_SIZE)
            );

        } catch (Exception e) {
            log.error(">>> Lua script execution 실패: {}", e.getMessage());
            return -3L;
        }
    }

    // 레이더 차트 분석용
    public List<GenreScoreInfo> getOrGenerateChart(Long userId, Supplier<List<GenreScoreInfo>> supplier) {

        String key = CHART_KEY_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<>() {});
            } catch (Exception e) {
                redisTemplate.delete(key);
            }
        }

        // 캐시 없으면 DB 조회
        List<GenreScoreInfo> data = supplier.get();
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data), Duration.ofDays(7));
        } catch (Exception e) {
            log.warn(">>> 취향분석 cache helper [chart cache write 실패]: {}", e.getMessage());
        }

        return data;
    }


    public void addPendingSwipe(Long userId, Long worksId, boolean isLiked) {

        PendingSwipeDto dto = PendingSwipeDto.builder()
                .userId(userId).worksId(worksId).isLiked(isLiked).build();

        try {
            String json = objectMapper.writeValueAsString(dto);

            // 유저별 리스트에 저장 (하루 유지)
            redisTemplate.opsForList().rightPush(PENDING_LIST_PREFIX + userId, json);
            redisTemplate.expire(PENDING_LIST_PREFIX + userId, Duration.ofDays(1));

            // 스케줄러 사용을 위함
            redisTemplate.opsForList().rightPush(GLOBAL_QUEUE_KEY, json);

        } catch (Exception e) {
            log.warn(">>> 취향분석 cache helper [pending]: {}", String.valueOf(e));
        }
    }

    // 유저별 pending data 전체 조회
    public List<PendingSwipeDto> getAllPendingSwipes(Long userId) {
        List<String> rawList = redisTemplate.opsForList().range(PENDING_LIST_PREFIX + userId, 0, -1);
        if (rawList == null) return Collections.emptyList();

        return rawList.stream()
                .map(s -> {
                    try { return objectMapper.readValue(s, PendingSwipeDto.class); }
                    catch (Exception e) { return null; }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public Set<Long> getPendingWorksIds(Long userId) {
        return getAllPendingSwipes(userId).stream()
                .map(PendingSwipeDto::worksId)
                .collect(Collectors.toSet());
    }

    // 오늘 완료 마킹 및 캐시 관리
    public boolean isAlreadyParticipatedToday(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(DONE_KEY_PREFIX + userId));
    }

    public void markAsParticipatedToday(Long userId) {
        Duration duration = Duration.between(LocalDateTime.now(), LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX));
        redisTemplate.opsForValue().set(DONE_KEY_PREFIX + userId, "true", duration);
    }

    public void deleteChartCache(Long userId) {
        redisTemplate.delete(CHART_KEY_PREFIX + userId);
    }

    public List<PendingSwipeDto> popBatchFromGlobalQueue(int count) {

        List<PendingSwipeDto> batch = new java.util.ArrayList<>();

        for (int i = 0; i < count; i++) {

            String json = redisTemplate.opsForList().leftPop(GLOBAL_QUEUE_KEY);
            if (json == null) break; // 큐가 비었으면 중단

            try {
                batch.add(objectMapper.readValue(json, PendingSwipeDto.class));
            } catch (Exception e) {
                log.warn(">>> 취향분석 cache helper [pop GQ for scheduler]{}", String.valueOf(e));
            }
        }
        return batch;
    }

    // 복구 로직
    public void rePushToGlobalQueue(PendingSwipeDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);

            // 큐의 앞쪽에 푸시되도록
            redisTemplate.opsForList().leftPush(GLOBAL_QUEUE_KEY, json);
        } catch (Exception e) {
            log.error("글로벌 큐 re-push 시도 fail: ", e);
        }
    }
}