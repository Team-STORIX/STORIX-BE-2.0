package com.storix.batch.scheduler;

import com.storix.domain.domains.user.adaptor.UserAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountSuspensionRestoreScheduler {

    private final UserAdaptor userAdaptor;

    /**
     * 매일 새벽 3시 실행.
     * User.suspendedUntil < now 인 SUSPENDED 유저를 복구한다.
     * ReportCase 상태와 독립적으로 동작하므로 케이스 reopen/처리 이력에 영향받지 않는다.
     * restore()는 단순 필드 변경 외 부수효과가 없어 엔티티 로딩 없이 벌크 UPDATE로 처리한다.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void restoreExpiredSuspensions() {
        int restoredCount = userAdaptor.restoreExpiredSuspensions(LocalDateTime.now());
        log.info(">>>> [Scheduler] 계정 정지 해제 완료: {}건", restoredCount);
    }
}
