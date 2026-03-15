package com.storix.storix_api.domains.preference.service;

import com.storix.storix_api.domains.preference.application.helper.ExplorationCacheHelper;
import com.storix.storix_api.domains.preference.domain.PreferenceExploration;
import com.storix.storix_api.domains.preference.dto.PendingSwipeDto;
import com.storix.storix_api.domains.preference.repository.ExplorationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExplorationBatchScheduler {

    private final ExplorationCacheHelper cacheHelper;
    private final ExplorationRepository explorationRepository;

    // 5분마다 실행
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void flushExplorationDataToDb() {

        int batchSize = 100;
        List<PendingSwipeDto> batch = cacheHelper.popBatchFromGlobalQueue(batchSize);

        if (batch.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        int totalCount = batch.size();

        // 중복 제거 필터링
        List<PreferenceExploration> entitiesToSave = batch.stream()
                .filter(dto -> {
                    boolean exists = explorationRepository.existsByUserIdAndWorksId(dto.userId(), dto.worksId());
                    if (exists) {
                        log.debug(">>> [Batch Skip] User {} - Works {} is already recorded.", dto.userId(), dto.worksId());
                    }
                    return !exists;
                })
                .map(dto -> PreferenceExploration.builder()
                        .userId(dto.userId())
                        .worksId(dto.worksId())
                        .isLiked(dto.isLiked())
                        .build())
                .toList();

        int skippedCount = totalCount - entitiesToSave.size();

        // 저장 로직 실행
        if (entitiesToSave.isEmpty()) {
            log.info(">>> [ExplorationBatch] 새 데이터 없음 (Total: {}, Skipped: {})", totalCount, skippedCount);
            return;
        }


        try {
            explorationRepository.saveAll(entitiesToSave);

            log.info(">>> [ExplorationBatch] synchronized 성 [Total: {}, Saved: {}, Skipped: {}] ({}ms)",
                    totalCount, entitiesToSave.size(), skippedCount, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error(">>> [ExplorationBatch] Critical save error: {}. Check DB constraints or entity mapping.", e.getMessage());
        }
    }
}