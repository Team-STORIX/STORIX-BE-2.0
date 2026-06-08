package com.storix.batch.scheduler;

import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountSuspensionRestoreScheduler {

    private final UserAdaptor userAdaptor;

    /**
     * 매일 새벽 3시 실행.
     * User.suspendedUntil < now 인 SUSPENDED 유저를 복구한다.
     * ReportCase 상태와 독립적으로 동작하므로 케이스 reopen/처리 이력에 영향받지 않는다.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void restoreExpiredSuspensions() {
        List<User> users = userAdaptor.findExpiredSuspensions(LocalDateTime.now());

        if (users.isEmpty()) {
            return;
        }

        for (User user : users) {
            user.restore();
        }

        log.info(">>>> [Scheduler] 계정 정지 해제 완료: {}건", users.size());
    }
}
