package com.storix.domain.domains.search.service;

import com.storix.domain.domains.search.dto.TrendingItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final StringRedisTemplate redisTemplate;

    // 날짜별 키 접두사
    private static final String TRENDING_KEY_PREFIX = "search:trending:";
    private static final String RECENT_KEY_PREFIX = "search:recent:";
    private static final String LIBRARY_RECENT_KEY_PREFIX = "library:recent";

    // 최근 검색어 개수 (10)
    private static final int MAX_RECENT_SIZE = 10;

    // 최근 검색어 TTL (14일)
    private static final long RECENT_KEY_TTL_DAYS = 14;

    // Redis 키 유효 기간 (랭킹 리셋)
    private static final long TRENDING_KEY_TTL_DAYS = 3;

    // 최소 검색 횟수 지정
    private static final double MIN_TRENDING_SCORE = 5.0;

    // 최근 검색어 저장을 위한 Lua Script
    // 1. LREM: 기존 키워드 삭제 (중복 방지)
    // 2. LPUSH: 최신 키워드 삽입
    // 3. LTRIM: 사이즈 제한 (0 ~ N-1)
    // 4. EXPIRE: TTL 갱신
    private static final String ADD_RECENT_SEARCH_SCRIPT =
            "redis.call('LREM', KEYS[1], 1, ARGV[1]); " +
                    "redis.call('LPUSH', KEYS[1], ARGV[1]); " +
                    "redis.call('LTRIM', KEYS[1], 0, ARGV[2]); " +
                    "redis.call('EXPIRE', KEYS[1], ARGV[3]); " +
                    "return 1;";

    // 홈
    /** 1. 검색어 저장 (인기 + 최근 검색어) */
    @Async("logThreadPool")
    public void addSearchLog(Long userId, String keyword) {
        try {
            if (keyword == null || keyword.isBlank()) return;

            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String todayKey = TRENDING_KEY_PREFIX + today;

            // 인기 검색어 점수 추가
            redisTemplate.opsForZSet().incrementScore(todayKey, keyword, 1.0);

            redisTemplate.expire(todayKey, TRENDING_KEY_TTL_DAYS, TimeUnit.DAYS);

            // 로그인한 유저: 최근 검색어 저장
            if (userId != null) {

                String key = RECENT_KEY_PREFIX + userId;

                // Lua Script
                // KEYS[1]: 키 이름
                // ARGV[1]: 검색어, ARGV[2]: 리스트 크기 제한(인덱스), ARGV[3]: TTL(초 단위)
                RedisScript<Long> script = new DefaultRedisScript<>(ADD_RECENT_SEARCH_SCRIPT, Long.class);

                redisTemplate.execute(script,
                        Collections.singletonList(key), // KEYS
                        keyword,                        // ARGV[1]
                        String.valueOf(MAX_RECENT_SIZE - 1), // ARGV[2] (trim index)
                        String.valueOf(TimeUnit.DAYS.toSeconds(RECENT_KEY_TTL_DAYS)) // ARGV[3] (seconds)
                );
            }
        } catch (Exception e) {
            log.error("검색어 로그 저장 실패: {}", e.getMessage());
        }
    }


    /** 2. 급상승 검색어 조회 (Top 10) */
    public List<TrendingItem> getTrendingKeywords() {

        LocalDate now = LocalDate.now();
        String todayKey = TRENDING_KEY_PREFIX + now.format(DateTimeFormatter.BASIC_ISO_DATE);
        String yesterdayKey = TRENDING_KEY_PREFIX + now.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);

        // 오늘 Top 10
        Set<ZSetOperations.TypedTuple<String>> currentKeywordsWithScores =
                redisTemplate.opsForZSet().reverseRangeWithScores(todayKey, 0, 9);

        if (currentKeywordsWithScores == null || currentKeywordsWithScores.isEmpty()) {
            return List.of();
        }

        List<TrendingItem> result = new ArrayList<>();
        int currentRank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : currentKeywordsWithScores) {

            String keyword = tuple.getValue();
            Double score = tuple.getScore();

            // 최소 점수 미만이면 결과에서 제외
            if (score == null || score < MIN_TRENDING_SCORE) {
                break;
            }

            // 어제 랭킹 (비교용)
            Long prevRankIndex = redisTemplate.opsForZSet().reverseRank(yesterdayKey, keyword);

            String status = "SAME";

            if (prevRankIndex == null) {
                status = "NEW";
            } else {
                int prevRank = prevRankIndex.intValue() + 1;

                if (prevRank > currentRank) { status = "UP"; }
                else if (prevRank < currentRank) { status = "DOWN"; }
            }

            result.add(TrendingItem.builder()
                    .keyword(keyword)
                    .rank(currentRank)
                    .status(status)
                    .build());

            currentRank++;
        }

        return result;
    }

    /** 3. 최근 검색어 조회 */
    public List<String> getRecentKeywords(Long userId) {

        String key = RECENT_KEY_PREFIX + userId;
        List<String> keywords = redisTemplate.opsForList().range(key, 0, MAX_RECENT_SIZE - 1);

        return keywords != null ? keywords : List.of();
    }

    /** 4. 최근 검색어 삭제 */
    public void deleteRecentKeyword(Long userId, String keyword) {

        String key = RECENT_KEY_PREFIX + userId;

        redisTemplate.opsForList().remove(key, 1, keyword);
    }

    /** 5. 추천 검색어 (검색 결과 없는 경우) */
    public String getFallbackRecommendation() {

        try {
            // 오늘 날짜 기준으로 급상승 키 생성
            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String todayKey = TRENDING_KEY_PREFIX + today;

            Set<String> rank11to20 = redisTemplate.opsForZSet().reverseRange(todayKey, 10, 19);

            if (rank11to20 == null || rank11to20.isEmpty()) {
                return null;
            }

            // 랜덤 선택 로직
            List<String> candidatesKeyword = new ArrayList<>(rank11to20);
            Collections.shuffle(candidatesKeyword);

            return candidatesKeyword.get(0);

        } catch (Exception e) {
            log.warn("[추천 검색어 조회 실패] ", e);
            return null;
        }
    }

    // 서재
    /** 1. 검색어 저장 (최근 검색어) */
    @Async("logThreadPool")
    public void addLibrarySearchLog(Long userId, String keyword) {
        try {
            if (keyword == null || keyword.isBlank()) return;

            String key = LIBRARY_RECENT_KEY_PREFIX + userId;

            // Lua Script
            // KEYS[1]: 키 이름
            // ARGV[1]: 검색어, ARGV[2]: 리스트 크기 제한(인덱스), ARGV[3]: TTL(초 단위)
            RedisScript<Long> script = new DefaultRedisScript<>(ADD_RECENT_SEARCH_SCRIPT, Long.class);

            redisTemplate.execute(script,
                    Collections.singletonList(key), // KEYS
                    keyword,                        // ARGV[1]
                    String.valueOf(MAX_RECENT_SIZE - 1), // ARGV[2] (trim index)
                    String.valueOf(TimeUnit.DAYS.toSeconds(RECENT_KEY_TTL_DAYS)) // ARGV[3] (seconds)
            );

        } catch (Exception e) {
            log.error("서재 검색어 로그 저장 실패: {}", e.getMessage());
        }
    }

    /** 2. 최근 검색어 조회 */
    public List<String> getLibraryRecentKeywords(Long userId) {

        String key = LIBRARY_RECENT_KEY_PREFIX + userId;
        List<String> keywords = redisTemplate.opsForList().range(key, 0, MAX_RECENT_SIZE - 1);

        return keywords != null ? keywords : List.of();
    }

    /** 3. 최근 검색어 삭제 */
    public void deleteLibraryRecentKeyword(Long userId, String keyword) {

        String key = LIBRARY_RECENT_KEY_PREFIX + userId;
        redisTemplate.opsForList().remove(key, 1, keyword);
    }
}
