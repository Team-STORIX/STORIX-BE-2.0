package com.storix.api.domain.search.listener;

import com.storix.domain.domains.search.exception.SearchReindexInProgressException;
import com.storix.domain.domains.search.service.FeedIndexService;
import com.storix.domain.domains.search.service.WorksIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexBootstrapListener {

    private static final int MAX_ATTEMPTS = 30;
    private static final Duration RETRY_INTERVAL = Duration.ofSeconds(10);

    private final WorksIndexService worksIndexService;
    private final FeedIndexService feedIndexService;

    // ES 가 앱보다 늦게 뜰 수 있어 몇 분 다시 시도한다. 그동안 앱 기동과 공용 스레드 풀을 막지 않게 따로 돌린다
    @EventListener(ApplicationReadyEvent.class)
    public void reindexIfMissing() {
        start("works", () -> {
            if (worksIndexService.isAliasMissing()) worksIndexService.reindexAll();
        });
        start("feeds", () -> {
            if (feedIndexService.isAliasMissing()) feedIndexService.reindexAll();
        });
    }

    private void start(String name, IndexTask task) {
        Thread thread = new Thread(() -> runWithRetry(name, task), name + "-index-bootstrap");
        thread.setDaemon(true);
        thread.start();
    }

    private void runWithRetry(String name, IndexTask task) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                task.run();
                return;
            } catch (SearchReindexInProgressException e) {
                return;
            } catch (Exception e) {
                log.warn(">>> [SearchIndex] 기동 시 색인 확인 실패 index={}, attempt={}, cause={}", name, attempt, e.getMessage());
            }

            try {
                Thread.sleep(RETRY_INTERVAL.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.error(">>> [SearchIndex] 기동 시 색인 포기 index={}, attempts={}", name, MAX_ATTEMPTS);
    }

    @FunctionalInterface
    private interface IndexTask {
        void run() throws Exception;
    }
}
