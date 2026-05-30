package com.storix.batch.scheduler;

import com.storix.domain.domains.report.adaptor.ReportCaseAdaptor;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        // 중복 userId 제거
        Set<Long> candidateUserIds = expired.stream()
                .map(ReportCase::getReportedUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 더 최근의 유효한 정지 케이스가 있는 userId 제외 (중복 정지 케이스 방어)
        List<Long> restorableUserIds = candidateUserIds.stream()
                .filter(userId -> !reportCaseAdaptor.hasActiveSuspension(userId, threshold))
                .toList();

        if (restorableUserIds.isEmpty()) {
            return;
        }

        // 배치 조회 (N+1 제거)
        List<User> users = userAdaptor.findSuspendedUsersByIds(restorableUserIds);

        for (User user : users) {
            user.restore();
        }

        log.info(">>>> [Scheduler] 계정 정지 해제 완료: {}건", users.size());
    }
}
