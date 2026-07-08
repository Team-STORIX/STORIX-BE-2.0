package com.storix.batch.scheduler;

import com.storix.domain.domains.report.adaptor.ReportRetentionAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserSanctionHistoryAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivacyRetentionScheduler {

    private static final int OAUTH_OID_RETENTION_YEARS = 1;
    private static final int REPORT_RETENTION_YEARS = 3;
    private static final int SANCTION_RETENTION_YEARS = 1;

    private final UserAdaptor userAdaptor;
    private final ReportRetentionAdaptor reportRetentionAdaptor;
    private final UserSanctionHistoryAdaptor userSanctionHistoryAdaptor;

    // 탈퇴 후 1년 경과한 소셜 식별자 파기
    @Scheduled(cron = "0 10 4 * * *")
    @Transactional
    public void purgeExpiredOauthOid() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(OAUTH_OID_RETENTION_YEARS);
        int purged = userAdaptor.purgeOauthOidBefore(cutoff);
        if (purged > 0) {
            log.info(">>>> [PrivacyRetentionScheduler] 소셜 식별자 파기 count={}, cutoff={}", purged, cutoff);
        }
    }

    // 처리 완료 후 3년 경과한 신고 기록 파기
    @Scheduled(cron = "0 20 4 * * *")
    public void purgeExpiredReports() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(REPORT_RETENTION_YEARS);
        int purged = reportRetentionAdaptor.purgeProcessedBefore(cutoff);
        if (purged > 0) {
            log.info(">>>> [PrivacyRetentionScheduler] 신고 기록 파기 count={}, cutoff={}", purged, cutoff);
        }
    }

    // 제재 종료 또는 탈퇴 후 1년 경과한 제재 기록 파기
    @Scheduled(cron = "0 30 4 * * *")
    @Transactional
    public void purgeExpiredSanctions() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(SANCTION_RETENTION_YEARS);
        int purged = userSanctionHistoryAdaptor.deleteExpiredBefore(cutoff);
        if (purged > 0) {
            log.info(">>>> [PrivacyRetentionScheduler] 제재 기록 파기 count={}, cutoff={}", purged, cutoff);
        }
    }
}
