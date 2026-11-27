package com.storix.domain.domains.library.listener;

import com.storix.domain.domains.library.event.LibraryCountChangedEvent;
import com.storix.domain.domains.library.service.LibraryCountCacheHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LibraryCountCacheEvictListener {

    private final LibraryCountCacheHelper libraryCountCacheHelper;

    // 커밋 전에 지우면 그사이 조회가 옛 값을 다시 캐싱한다
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(LibraryCountChangedEvent event) {
        libraryCountCacheHelper.evictReviewCount(event.userId());
    }
}
