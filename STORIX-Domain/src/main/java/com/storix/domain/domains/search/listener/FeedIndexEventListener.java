package com.storix.domain.domains.search.listener;

import com.storix.domain.domains.search.event.FeedIndexEvent;
import com.storix.domain.domains.search.service.FeedIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FeedIndexEventListener {

    private final FeedIndexService feedIndexService;

    // 커밋된 게시글만 색인하고, 응답이 ES 를 기다리지 않게 비동기로 돌린다
    @Async("logThreadPool")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(FeedIndexEvent event) {
        if (event.deleted()) {
            feedIndexService.deleteBoard(event.boardId());
        } else {
            feedIndexService.indexBoard(event.boardId());
        }
    }
}
