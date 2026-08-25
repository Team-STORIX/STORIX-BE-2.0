package com.storix.batch.scheduler;

import com.storix.domain.domains.adultverification.adaptor.AdultVerificationAdaptor;
import com.storix.domain.domains.adultverification.service.AdultVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdultVerificationScheduler {

    private static final int ABANDONED_HOURS = 1;

    private final AdultVerificationService adultVerificationService;
    private final AdultVerificationAdaptor adultVerificationAdaptor;

    @Scheduled(cron = "0 50 4 * * *", zone = "Asia/Seoul")
    public void expireOverdue() {
        int expired = adultVerificationService.expireOverdue();
        if (expired > 0) {
            log.info(">>>> [AdultVerificationScheduler] 성인인증 만료 처리 count={}", expired);
        }
    }

    @Scheduled(cron = "0 0 */2 * * *", zone = "Asia/Seoul")
    @Transactional
    public void markAbandoned() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(ABANDONED_HOURS);
        int abandoned = adultVerificationAdaptor.markAbandonedBefore(threshold);
        if (abandoned > 0) {
            log.info(">>>> [AdultVerificationScheduler] 방치된 인증 요청 정리 count={} threshold={}", abandoned, threshold);
        }
    }
}
