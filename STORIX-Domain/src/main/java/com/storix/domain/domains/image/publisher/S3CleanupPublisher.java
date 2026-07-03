package com.storix.domain.domains.image.publisher;

import com.storix.domain.domains.image.event.S3CleanupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 도메인 서비스/어댑터에서 S3 오브젝트 정리 이벤트 발행.
 * 트랜잭션 안에서 호출해야 커밋 이후에만 삭제가 실행된다 (롤백 시 미실행).
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3CleanupPublisher {

    private final ApplicationEventPublisher eventPublisher;

    // 커밋 후 S3에서 삭제할 objectKey 다건 발행
    public void publish(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return;
        }

        List<String> keys = objectKeys.stream()
                .filter(key -> key != null && !key.isBlank())
                .toList();

        if (keys.isEmpty()) {
            return;
        }

        try {
            eventPublisher.publishEvent(new S3CleanupEvent(keys));
        } catch (Exception e) {
            log.warn(">>> [S3Cleanup] publish failed keyCount={}, cause={}", keys.size(), e.getMessage());
        }
    }

    // 커밋 후 S3에서 삭제할 objectKey 단건 발행
    public void publish(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        publish(List.of(objectKey));
    }
}
