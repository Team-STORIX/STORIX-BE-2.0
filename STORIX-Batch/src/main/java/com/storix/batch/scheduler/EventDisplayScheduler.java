package com.storix.batch.scheduler;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.event.service.BannerService;
import com.storix.domain.domains.event.service.EventContentCacheHelper;
import com.storix.domain.domains.event.service.PopupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDisplayScheduler {

    private final PopupService eventPopupService;
    private final BannerService eventBannerService;
    private final EventContentCacheHelper eventContentCacheHelper;

    @Scheduled(cron = "0 0/10 * * * *", zone = "Asia/Seoul")
    public void transitionDisplayStatuses() {
        LocalDateTime now = LocalDateTime.now();

        int popupActivated = eventPopupService.activateDuePopups(now);
        int popupEnded = eventPopupService.endExpiredPopups(now);
        int bannerActivated = eventBannerService.activateDueBanners(now);
        int bannerEnded = eventBannerService.endExpiredBanners(now);

        // 커밋 후 활성 상태가 바뀐 도메인만 캐시 무효화
        if (popupActivated > 0 || popupEnded > 0) {
            eventContentCacheHelper.evict(STORIXStatic.ACTIVE_POPUP_KEY);
        }
        if (bannerActivated > 0 || bannerEnded > 0) {
            eventContentCacheHelper.evict(STORIXStatic.ACTIVE_BANNER_KEY);
        }

        if (popupActivated > 0 || popupEnded > 0 || bannerActivated > 0 || bannerEnded > 0) {
            log.info(">>> [EventDisplayScheduler] popup(activated={}, ended={}), banner(activated={}, ended={})",
                    popupActivated, popupEnded, bannerActivated, bannerEnded);
        }
    }
}
