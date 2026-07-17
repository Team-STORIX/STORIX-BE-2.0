package com.storix.batch.scheduler;

import com.storix.domain.domains.pushdevice.adaptor.PushDeviceAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
public class StalePushDeviceScheduler {

    private static final int INACTIVE_DAYS = 29;

    private final PushDeviceAdaptor pushDeviceAdaptor;

    @Scheduled(cron = "0 40 4 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deactivateStaleDevices() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(INACTIVE_DAYS);
        int deactivated = pushDeviceAdaptor.deactivateStaleDevices(threshold);
        if (deactivated > 0) {
            log.info(">>>> [StalePushDeviceScheduler] 미활동 디바이스 푸시 비활성화 count={}, threshold={}", deactivated, threshold);
        }
    }
}
