package com.storix.batch.scheduler;

import static com.storix.common.utils.STORIXStatic.TRENDING_AGGREGATED_KEY;
import static com.storix.common.utils.STORIXStatic.TRENDING_PREV_AGGREGATED_KEY;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrendingKeywordScheduler {

    private final StringRedisTemplate redisTemplate;

    private static final String TRENDING_KEY_PREFIX = "search:trending:";

    /** 최근 3일 합산 키 갱신 (20분 주기) — 오늘 + 이전 합산(어제+그저께) */
    @Scheduled(cron = "0 */20 * * * *")
    public void refreshTrendingAggregation() {
        log.info(">>>> [Scheduler] 인기 검색어 합산 키 갱신 시작");

        LocalDate now = LocalDate.now();
        String todayKey = TRENDING_KEY_PREFIX + now.format(DateTimeFormatter.BASIC_ISO_DATE);

        redisTemplate.opsForZSet().unionAndStore(todayKey, List.of(TRENDING_PREV_AGGREGATED_KEY), TRENDING_AGGREGATED_KEY);
        redisTemplate.expire(TRENDING_AGGREGATED_KEY, 2, TimeUnit.DAYS);

        log.info(">>>> [Scheduler] 인기 검색어 합산 키 갱신 완료");
    }

    /** 이전 비교 기준 합산 키 갱신 (매일 자정) */
    @Scheduled(cron = "0 0 0 * * *")
    public void refreshPrevAggregation() {
        log.info(">>>> [Scheduler] 이전 비교 기준 합산 키 갱신 시작");

        LocalDate now = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.BASIC_ISO_DATE;

        String yesterdayKey = TRENDING_KEY_PREFIX + now.minusDays(1).format(fmt);
        String twoDaysAgoKey = TRENDING_KEY_PREFIX + now.minusDays(2).format(fmt);

        redisTemplate.opsForZSet().unionAndStore(yesterdayKey, List.of(twoDaysAgoKey), TRENDING_PREV_AGGREGATED_KEY);
        redisTemplate.expire(TRENDING_PREV_AGGREGATED_KEY, 2, TimeUnit.DAYS);

        log.info(">>>> [Scheduler] 이전 비교 기준 합산 키 갱신 완료");
    }
}
