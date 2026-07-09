package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.BannerAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.Banner;
import com.storix.domain.domains.event.domain.BannerStatus;
import com.storix.domain.domains.event.domain.ContentTargetType;
import com.storix.domain.domains.event.dto.BannerCommand;
import com.storix.domain.domains.event.dto.BannerResponse;
import com.storix.domain.domains.event.dto.DisplayPeriod;
import com.storix.domain.domains.event.exception.BannerAppEventRequiredException;
import com.storix.domain.domains.event.exception.BannerInvalidDisplayPeriodException;
import com.storix.domain.domains.event.exception.BannerOutOfEventPeriodException;
import com.storix.domain.domains.event.exception.BannerOverlappingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private static final int BANNER_PAGE_SIZE = 10;
    private static final int MAX_ACTIVE_BANNERS = 3;

    private final BannerAdaptor eventBannerAdaptor;
    private final AppEventAdaptor appEventAdaptor;
    private final EventDisplayPeriodHelper eventDisplayPeriodHelper;

    @Transactional
    public Banner create(BannerCommand cmd, Long adminUserId) {
        validatePeriod(cmd.displayStartAt(), cmd.displayEndAt());
        validateAppEventRequired(cmd.contentTargetType(), cmd.appEventId() != null);
        // appEventId 없으면 독립 배너, 있으면 이벤트 기간으로 clamp
        AppEvent appEvent = cmd.appEventId() == null ? null : appEventAdaptor.findById(cmd.appEventId());
        DisplayPeriod period = clampToAppEvent(appEvent, cmd.displayStartAt(), cmd.displayEndAt());
        validateOverlapWithinLimit(period.start(), period.end(), null);
        return eventBannerAdaptor.save(Banner.builder()
                .appEvent(appEvent)
                .contentTargetType(cmd.contentTargetType())
                .bannerTitle(cmd.bannerTitle())
                .imageObjectKey(cmd.imageObjectKey())
                .displayStartAt(period.start())
                .displayEndAt(period.end())
                .assigneeAdminId(adminUserId)
                .build());
    }

    @Transactional
    public Banner update(Long bannerId, BannerCommand cmd) {
        validatePeriod(cmd.displayStartAt(), cmd.displayEndAt());
        Banner banner = getById(bannerId);
        // appEvent 는 불변 - 기존 배너가 소속된 이벤트 기준으로 검증/clamp
        validateAppEventRequired(cmd.contentTargetType(), banner.getAppEvent() != null);
        DisplayPeriod period = clampToAppEvent(banner.getAppEvent(), cmd.displayStartAt(), cmd.displayEndAt());
        // 종료된 배너는 다시 노출되지 않으므로 동시 노출 상한 검증 대상에서 제외
        if (banner.getStatus() != BannerStatus.ENDED) {
            validateOverlapWithinLimit(period.start(), period.end(), bannerId);
        }
        banner.update(
                cmd.contentTargetType(),
                cmd.bannerTitle(),
                cmd.imageObjectKey(),
                period.start(),
                period.end()
        );
        return banner;
    }

    @Transactional(readOnly = true)
    public Banner getById(Long bannerId) {
        return eventBannerAdaptor.findById(bannerId);
    }

    @Transactional(readOnly = true)
    public Page<Banner> getBanners(int page) {
        return eventBannerAdaptor.findAll(PageRequest.of(page, BANNER_PAGE_SIZE));
    }

    // objectKey 형태로 반환, baseUrl/캐시는 UseCase 담당
    @Transactional(readOnly = true)
    public List<BannerResponse> findActiveBanners(LocalDateTime now) {
        return eventBannerAdaptor.findActiveBanners(now, MAX_ACTIVE_BANNERS).stream()
                .map(BannerResponse::from)
                .toList();
    }

    @Transactional
    public Banner cancel(Long bannerId) {
        Banner banner = getById(bannerId);
        banner.end();
        return banner;
    }

    @Transactional
    public int activateDueBanners(LocalDateTime now) {
        List<Banner> banners = eventBannerAdaptor.findDueToActivate(now);
        banners.forEach(Banner::activate);
        return banners.size();
    }

    @Transactional
    public int endExpiredBanners(LocalDateTime now) {
        List<Banner> banners = eventBannerAdaptor.findDueToEnd(now);
        banners.forEach(Banner::end);
        return banners.size();
    }

    // AppEvent 종료 cascade: 소속 활성 배너 일괄 종료
    @Transactional
    public void endByAppEvent(Long appEventId) {
        eventBannerAdaptor.findActiveByAppEvent(appEventId).forEach(Banner::end);
    }

    // AppEvent 기간 변경 cascade: 소속 배너 노출기간을 이벤트 기간 안으로 clamp
    @Transactional
    public void clampByAppEvent(Long appEventId, LocalDateTime eventStartAt, LocalDateTime eventEndAt) {
        eventBannerAdaptor.findActiveByAppEvent(appEventId)
                .forEach(banner -> banner.clampToEventPeriod(eventStartAt, eventEndAt));
    }

    private void validatePeriod(LocalDateTime displayStartAt, LocalDateTime displayEndAt) {
        eventDisplayPeriodHelper.validate(displayStartAt, displayEndAt, () -> BannerInvalidDisplayPeriodException.EXCEPTION);
    }

    private void validateAppEventRequired(ContentTargetType contentTargetType, boolean hasAppEvent) {
        eventDisplayPeriodHelper.requireAppEventForType(contentTargetType, hasAppEvent, () -> BannerAppEventRequiredException.EXCEPTION);
    }

    private DisplayPeriod clampToAppEvent(AppEvent appEvent, LocalDateTime displayStartAt, LocalDateTime displayEndAt) {
        return eventDisplayPeriodHelper.clampToAppEvent(appEvent, displayStartAt, displayEndAt, () -> BannerOutOfEventPeriodException.EXCEPTION);
    }

    // 겹치는 종료전 배너가 이미 상한이면 거부 (새 배너 포함 최대 MAX_ACTIVE_BANNERS)
    private void validateOverlapWithinLimit(LocalDateTime displayStartAt, LocalDateTime displayEndAt, Long excludeId) {
        List<DisplayPeriod> overlapping = eventBannerAdaptor.findOverlappingPeriods(displayStartAt, displayEndAt, excludeId);
        if (eventDisplayPeriodHelper.maxConcurrent(overlapping, displayStartAt) >= MAX_ACTIVE_BANNERS) {
            throw BannerOverlappingException.EXCEPTION;
        }
    }
}
