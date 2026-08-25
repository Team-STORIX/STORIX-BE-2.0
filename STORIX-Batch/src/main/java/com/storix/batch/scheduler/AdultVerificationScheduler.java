package com.storix.batch.scheduler;

import com.storix.domain.domains.adultverification.domain.AdultVerification;
import com.storix.domain.domains.adultverification.dto.IdentityVerificationResult;
import com.storix.domain.domains.adultverification.service.AdultVerificationService;
import com.storix.infrastructure.external.portone.IdentityVerificationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdultVerificationScheduler {

    private static final int ABANDONED_HOURS = 1;
    private static final int SETTLE_LIMIT = 200;

    private final AdultVerificationService adultVerificationService;
    private final IdentityVerificationHelper identityVerificationHelper;

    @Scheduled(cron = "0 50 4 * * *", zone = "Asia/Seoul")
    public void expireOverdue() {
        int expired = adultVerificationService.expireOverdue();
        if (expired > 0) {
            log.info(">>>> [AdultVerificationScheduler] 성인인증 만료 처리 count={}", expired);
        }
    }

    // 방치로 보이는 건 중에는 인증을 마치고 확정만 유실된 것이 섞여 있다
    // 그냥 정리하면 이미 과금된 인증이 사라지므로 포트원에 하나씩 물어보고 나눈다
    @Scheduled(cron = "0 0 */2 * * *", zone = "Asia/Seoul")
    public void settleAbandoned() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(ABANDONED_HOURS);
        List<AdultVerification> candidates =
                adultVerificationService.findAbandonCandidates(threshold, SETTLE_LIMIT);
        if (candidates.isEmpty()) {
            return;
        }

        int recovered = 0;
        int abandoned = 0;
        for (AdultVerification candidate : candidates) {
            String identityVerificationId = candidate.getIdentityVerificationId();
            try {
                IdentityVerificationResult result =
                        identityVerificationHelper.findVerification(identityVerificationId);

                if (result != null && result.isVerified()) {
                    adultVerificationService.confirm(candidate.getUserId(), identityVerificationId, result);
                    recovered++;
                    log.warn(">>>> [AdultVerificationScheduler] 확정 유실 복구 userId={} identityVerificationId={}",
                            candidate.getUserId(), identityVerificationId);
                    continue;
                }

                adultVerificationService.abandon(identityVerificationId);
                abandoned++;
            } catch (Exception e) {
                // 한 건이 실패해도 나머지는 계속 본다. 다음 주기에 다시 후보로 잡힌다
                log.error(">>>> [AdultVerificationScheduler] 정리 실패 identityVerificationId={} cause={}",
                        identityVerificationId, e.getMessage());
            }
        }

        log.info(">>>> [AdultVerificationScheduler] 방치된 인증 요청 정리 abandoned={} recovered={} threshold={}",
                abandoned, recovered, threshold);
    }
}
