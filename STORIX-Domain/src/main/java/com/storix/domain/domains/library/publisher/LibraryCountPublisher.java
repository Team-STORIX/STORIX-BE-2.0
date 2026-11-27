package com.storix.domain.domains.library.publisher;

import com.storix.domain.domains.library.event.LibraryCountChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LibraryCountPublisher {

    private final ApplicationEventPublisher eventPublisher;

    // 서재 작품 수가 바뀌는 트랜잭션 안에서 부른다. 캐시는 커밋 뒤에 지운다
    public void publishChanged(Long userId) {
        eventPublisher.publishEvent(new LibraryCountChangedEvent(userId));
    }
}
