package com.storix.domain.domains.event.listener;

import com.storix.domain.domains.event.service.UserAppEventService;
import com.storix.domain.domains.user.event.TitleAcquiredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TitleAcquiredEventListener {

    private final UserAppEventService userAppEventService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TitleAcquiredEvent event) {
        userAppEventService.createTitleAcquiredEvent(event.userId(), event.title(), event.acquiredAt());
    }
}
