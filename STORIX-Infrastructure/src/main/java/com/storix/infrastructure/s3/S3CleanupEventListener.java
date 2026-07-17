package com.storix.infrastructure.s3;

import com.storix.domain.domains.image.event.S3CleanupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3CleanupEventListener {

    private final S3ObjectDeleter s3ObjectDeleter;

    @Async("s3CleanupExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEvent(S3CleanupEvent event) {
        s3ObjectDeleter.deleteObjects(event.objectKeys());
    }
}
