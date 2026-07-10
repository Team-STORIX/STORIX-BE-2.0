package com.storix.batch.scheduler;

import com.storix.domain.domains.genrescore.service.GenreScoreAggregationService;
import com.storix.domain.domains.genrescore.service.GenreScoreAggregationService.ChunkResult;
import com.storix.domain.domains.user.service.UserTitleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreScoreAggregationScheduler {

    private static final int CHUNK_SIZE = GenreScoreAggregationService.DEFAULT_CHUNK_SIZE;
    private static final long INTER_CHUNK_DELAY_MS = 100L;
    private static final int MAX_ITERATIONS = 10_000;

    private final GenreScoreAggregationService aggregationService;
    private final UserTitleService userTitleService;

    // 5분마다 미처리 로그 청크 단위로 집계
    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul")
    public void run() {
        long started = System.currentTimeMillis();
        Set<Long> touchedUsers = new HashSet<>();
        int totalLogs = 0;
        int totalGroups = 0;
        int iter = 0;

        while (iter++ < MAX_ITERATIONS) {
            ChunkResult result = aggregationService.processChunk(CHUNK_SIZE);
            if (result.processedLogs() == 0) break;

            totalLogs += result.processedLogs();
            totalGroups += result.groups();
            touchedUsers.addAll(result.users());

            sleep(INTER_CHUNK_DELAY_MS);
        }

        if (iter >= MAX_ITERATIONS) {
            log.warn(">>> [GenreScoreBatch] MAX_ITERATIONS({}) 도달 — 다음 실행 시 잔여분 처리", MAX_ITERATIONS);
        }

        // 집계로 점수가 변동된 유저의 칭호 일괄 갱신 (단일 트랜잭션 + JDBC 배치)
        // 실패해도 멱등 — 다음 재집계/기동 backfill 로 보정. 복구용으로 실패 userId 만 남긴다.
        try {
            userTitleService.assignTitles(touchedUsers);
        } catch (Exception e) {
            log.error(">>> [GenreScoreBatch] 칭호 일괄 갱신 실패 (count={}, userIds={})",
                    touchedUsers.size(), touchedUsers, e);
        }

        int oldDeleted = aggregationService.cleanupOldProcessed();

        log.info(">>> [GenreScoreBatch] processedLogs={}, groups={}, users={}, oldDeleted={}, iterations={}, took={}ms",
                totalLogs, totalGroups, touchedUsers.size(), oldDeleted, iter - 1,
                System.currentTimeMillis() - started);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
