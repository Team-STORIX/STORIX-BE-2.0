package com.storix.domain.domains.bannedword.listener;

import com.storix.domain.domains.bannedword.event.BannedWordChangedEvent;
import com.storix.domain.domains.bannedword.service.BannedWordMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BannedWordCacheReloadListener {

    private final BannedWordMatcher bannedWordMatcher;

    // 변경 트랜잭션이 커밋된 후에만 캐시를 갱신 — 롤백된 변경이 캐시에 반영되지 않도록 한다
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BannedWordChangedEvent event) {
        try {
            bannedWordMatcher.reload();
        } catch (Exception e) {
            log.error(">>> [BannedWord] 커밋 후 캐시 갱신 실패 — 수동 갱신(POST /api/v1/admin/banned-words/reload) 필요. cause={}", e.getMessage());
        }
    }
}
