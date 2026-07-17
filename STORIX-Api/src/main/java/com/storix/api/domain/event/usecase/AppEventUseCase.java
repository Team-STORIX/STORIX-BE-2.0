package com.storix.api.domain.event.usecase;

import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.event.dto.BannerResponse;
import com.storix.domain.domains.event.dto.OneTimeAppEventResponse;
import com.storix.domain.domains.event.dto.PopupResponse;
import com.storix.domain.domains.event.service.BannerService;
import com.storix.domain.domains.event.service.EventContentCacheHelper;
import com.storix.domain.domains.event.service.PopupDismissService;
import com.storix.domain.domains.event.service.PopupService;
import com.storix.domain.domains.event.service.UserAppEventCacheHelper;
import com.storix.domain.domains.event.service.UserAppEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AppEventUseCase {

    private final PopupService eventPopupService;
    private final PopupDismissService popupDismissService;
    private final BannerService eventBannerService;
    private final EventContentCacheHelper eventContentCacheHelper;
    private final UserAppEventService userAppEventService;
    private final UserAppEventCacheHelper userAppEventCacheHelper;

    @Value("${AWS_S3_BASE_URL}") private String baseUrl;

    // 노출 중인 팝업 조회
    public CustomResponse<PopupResponse> getActivePopup(Long userId) {

        LocalDate today = LocalDate.now();
        PopupResponse popup = eventContentCacheHelper.getActive(STORIXStatic.ACTIVE_POPUP_KEY, PopupResponse.class,
                        () -> eventPopupService.findActivePopup(LocalDateTime.now()), PopupResponse::displayEndAt)
                .filter(p -> !popupDismissService.isSuppressed(userId, p.id(), p.exposurePolicy(), today))
                .map(p -> p.withBaseUrl(baseUrl))
                .orElse(null);
        return CustomResponse.onSuccess(SuccessCode.APP_EVENTS_LOAD_SUCCESS, popup);
    }

    // 팝업 오늘 다시 안 보기
    public CustomResponse<Void> dismissPopupForToday(Long userId, Long popupId) {

        // 1. 팝업 존재 검증
        eventPopupService.getById(popupId);

        // 2. 오늘 다시 안 보기 처리
        popupDismissService.dismissForToday(userId, popupId, LocalDate.now());
        return CustomResponse.onSuccess(SuccessCode.APP_EVENT_POPUP_DISMISS_SUCCESS);
    }

    // 팝업 다시 보지 않기 (노출 기간 내내)
    public CustomResponse<Void> dismissPopupForever(Long userId, Long popupId) {

        // 1. 팝업 존재 검증
        eventPopupService.getById(popupId);

        // 2. 다시 보지 않기 처리
        popupDismissService.dismissForever(userId, popupId, LocalDate.now());
        return CustomResponse.onSuccess(SuccessCode.APP_EVENT_POPUP_NEVER_SHOW_SUCCESS);
    }

    // 노출 중인 배너 조회
    public CustomResponse<List<BannerResponse>> getActiveBanner() {

        List<BannerResponse> banners = eventContentCacheHelper.getActiveList(STORIXStatic.ACTIVE_BANNER_KEY, BannerResponse.class,
                        () -> eventBannerService.findActiveBanners(LocalDateTime.now()), BannerResponse::displayEndAt)
                .stream()
                .map(b -> b.withBaseUrl(baseUrl))
                .toList();
        return CustomResponse.onSuccess(SuccessCode.APP_EVENTS_LOAD_SUCCESS, banners);
    }

    // 칭호 획득 조회
    public CustomResponse<List<OneTimeAppEventResponse>> getPendingAcquiredTitleEvents(Long userId) {

        return CustomResponse.onSuccess(
                SuccessCode.APP_EVENTS_LOAD_SUCCESS,
                userAppEventService.getPendingEvents(userId)
        );
    }

    // 칭호 획득 확인 처리
    public CustomResponse<Void> ackAcquiredTitleEvent(Long userId, Long eventId) {

        if (userAppEventService.ack(userId, eventId)) {
            userAppEventCacheHelper.evict(userId);
        }
        return CustomResponse.onSuccess(SuccessCode.APP_EVENT_ACK_SUCCESS);
    }
}
