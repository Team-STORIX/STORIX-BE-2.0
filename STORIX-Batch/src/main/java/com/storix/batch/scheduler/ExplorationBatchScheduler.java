package com.storix.batch.scheduler;

import com.storix.domain.domains.preference.dto.PendingSwipeDto;
import com.storix.domain.domains.preference.service.ExplorationCacheHelper;
import com.storix.domain.domains.preference.service.ExplorationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExplorationBatchScheduler {

    private static final int BATCH_SIZE = 100;

    private final ExplorationService explorationService;
    private final ExplorationCacheHelper explorationCacheHelper;

    // 5분마다 실행
    @Scheduled(fixedDelay = 300000)
    public void flushExplorationDataToDb() {
        List<PendingSwipeDto> batch = explorationCacheHelper.popBatchFromGlobalQueue(BATCH_SIZE);
        explorationService.flushPendingSwipes(batch);
    }
}
