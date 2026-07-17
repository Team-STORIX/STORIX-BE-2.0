package com.storix.domain.domains.event.listener;

import com.storix.domain.domains.event.service.UserAppEventCacheHelper;
import com.storix.domain.domains.event.service.UserAppEventService;
import com.storix.domain.domains.user.event.TitleAcquiredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TitleAcquiredEventListener {

    private final UserAppEventService userAppEventService;
    private final UserAppEventCacheHelper userAppEventCacheHelper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TitleAcquiredEvent event) {
        try {
            userAppEventService.createTitleAcquiredEvent(event.userId(), event.title(), event.acquiredAt());
            userAppEventCacheHelper.evict(event.userId());
        } catch (Exception e) {
            log.warn(">>> [TitleAcquiredEventListener] 칭호 획득 이벤트 적재 실패. userId={}, title={}",
                    event.userId(), event.title(), e);
        }
    }
}
