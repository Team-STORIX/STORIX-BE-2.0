package com.storix.batch.scheduler;

import com.storix.common.utils.RedisKeyStatic;
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
        // 팝업/배너를 격리 처리해, 한쪽 실패가 다른 쪽 전환·캐시 무효화를 막지 않도록 한다
        transitionPopups(now);
        transitionBanners(now);
    }

    private void transitionPopups(LocalDateTime now) {
        boolean changed = false;
        try {
            int activated = eventPopupService.activateDuePopups(now);
            if (activated > 0) changed = true;
            int ended = eventPopupService.endExpiredPopups(now);
            if (ended > 0) changed = true;
            if (changed) {
                log.info(">>> [EventDisplayScheduler] popup(activated={}, ended={})", activated, ended);
            }
        } catch (Exception e) {
            log.error(">>> [EventDisplayScheduler] popup 전환 실패", e);
        } finally {
            // 앞 단계가 커밋됐으면 뒤 단계가 실패해도 캐시는 무효화
            if (changed) {
                eventContentCacheHelper.evict(RedisKeyStatic.Event.ACTIVE_POPUP);
            }
        }
    }

    private void transitionBanners(LocalDateTime now) {
        boolean changed = false;
        try {
            int activated = eventBannerService.activateDueBanners(now);
            if (activated > 0) changed = true;
            int ended = eventBannerService.endExpiredBanners(now);
            if (ended > 0) changed = true;
            if (changed) {
                log.info(">>> [EventDisplayScheduler] banner(activated={}, ended={})", activated, ended);
            }
        } catch (Exception e) {
            log.error(">>> [EventDisplayScheduler] banner 전환 실패", e);
        } finally {
            if (changed) {
                eventContentCacheHelper.evict(RedisKeyStatic.Event.ACTIVE_BANNER);
            }
        }
    }
}
