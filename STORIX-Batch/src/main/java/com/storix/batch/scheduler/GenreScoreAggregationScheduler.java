package com.storix.batch.scheduler;

import com.storix.domain.domains.genrescore.service.GenreScoreAggregationService;
import com.storix.domain.domains.genrescore.service.GenreScoreAggregationService.ChunkResult;
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


    // 매일 자정, 미처리 로그 청크 단위로 집계
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
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
