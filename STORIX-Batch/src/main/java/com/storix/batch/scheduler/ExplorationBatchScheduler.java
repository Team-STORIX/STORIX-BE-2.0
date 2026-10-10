package com.storix.batch.scheduler;

import com.storix.domain.domains.preference.service.ExplorationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExplorationBatchScheduler {

    private static final int BATCH_SIZE = 100;

    private final ExplorationService explorationService;

    // 5분마다 실행
    @Scheduled(fixedDelay = 300000)
    public void flushExplorationDataToDb() {
        explorationService.flushPendingSwipes(BATCH_SIZE);
    }
}
