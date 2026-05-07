package com.storix.domain.domains.genrescore.listener;

import com.storix.domain.domains.genrescore.domain.UserGenreScoreLog;
import com.storix.domain.domains.genrescore.event.GenreScoreEvent;
import com.storix.domain.domains.genrescore.repository.UserGenreScoreLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreScoreLogListener {

    private final UserGenreScoreLogRepository logRepository;

    // 트랜잭션 커밋 후 반영
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(GenreScoreEvent event) {
        if (event.userId() == null || event.genre() == null) {
            log.warn(">>> [GenreScore] invalid event dropped: {}", event);
            return;
        }

        try {
            logRepository.save(UserGenreScoreLog.from(event));
        } catch (Exception e) {
            log.error(">>> [GenreScore] log persist failed for event={}, cause={}", event, e.getMessage());
        }
    }
}
