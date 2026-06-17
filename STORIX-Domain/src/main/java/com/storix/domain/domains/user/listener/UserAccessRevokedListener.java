package com.storix.domain.domains.user.listener;

import com.storix.domain.domains.user.adaptor.TokenAdaptor;
import com.storix.domain.domains.user.adaptor.UserBlacklistAdaptor;
import com.storix.domain.domains.user.event.UserAccessRevokedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAccessRevokedListener {

    private final TokenAdaptor tokenAdaptor;
    private final UserBlacklistAdaptor userBlacklistAdaptor;

    // 트랜잭션 커밋 후 Redis 반영 (refreshToken 삭제 + blacklist 등록)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserAccessRevokedEvent event) {
        try {
            tokenAdaptor.deleteRefreshTokenByUserIdIfPresent(event.userId());
            switch (event.type()) {
                case SUSPENDED -> userBlacklistAdaptor.blockSuspended(event.userId(), event.suspendedUntil());
                case WITHDRAWN -> userBlacklistAdaptor.blockDeleted(event.userId());
            }
        } catch (Exception e) {
            log.error(">>> [UserAccessRevoked] redis sync failed for event={}, cause={}", event, e.getMessage());
        }
    }
}
