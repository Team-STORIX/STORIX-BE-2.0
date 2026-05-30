package com.storix.batch.scheduler;

import com.storix.domain.domains.report.adaptor.ReportCaseAdaptor;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.AccountState;
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

    private static final int SUSPENSION_DAYS = 7;

    private final ReportCaseAdaptor reportCaseAdaptor;
    private final UserAdaptor userAdaptor;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    @Transactional
    public void restoreExpiredSuspensions() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(SUSPENSION_DAYS);
        List<ReportCase> expired = reportCaseAdaptor.findExpiredSuspensions(threshold);

        if (expired.isEmpty()) {
            return;
        }

        int restored = 0;
        for (ReportCase reportCase : expired) {
            if (reportCase.getReportedUserId() == null) continue;

            User user = userAdaptor.findUserById(reportCase.getReportedUserId());
            if (user.getAccountState() == AccountState.SUSPENDED) {
                user.restore();
                restored++;
            }
        }

        log.info(">>>> [Scheduler] 계정 정지 해제 완료: {}건", restored);
    }
}
