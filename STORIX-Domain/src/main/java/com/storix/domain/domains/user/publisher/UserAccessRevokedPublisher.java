package com.storix.domain.domains.user.publisher;

import com.storix.domain.domains.user.event.UserAccessRevokedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAccessRevokedPublisher {

    private final ApplicationEventPublisher eventPublisher;

    // 계정 정지 - 트랜잭션 커밋 후 refreshToken 삭제 및 blacklist 등록
    public void publishSuspended(Long userId, LocalDateTime suspendedUntil) {
        try {
            eventPublisher.publishEvent(UserAccessRevokedEvent.suspended(userId, suspendedUntil));
        } catch (Exception e) {
            log.warn(">>> [UserAccessRevoked] publish failed userId={}, type=SUSPENDED, cause={}", userId, e.getMessage());
        }
    }

    // 계정 탈퇴 - 트랜잭션 커밋 후 refreshToken 삭제 및 blacklist 등록
    public void publishWithdrawn(Long userId) {
        try {
            eventPublisher.publishEvent(UserAccessRevokedEvent.withdrawn(userId));
        } catch (Exception e) {
            log.warn(">>> [UserAccessRevoked] publish failed userId={}, type=WITHDRAWN, cause={}", userId, e.getMessage());
        }
    }
}
