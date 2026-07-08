package com.storix.api.domain.event.usecase;

import com.storix.api.domain.event.controller.dto.AppEventRequest;
import com.storix.common.payload.PageResponseWrapperDTO;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.event.dto.AppEventResponse;
import com.storix.domain.domains.event.service.AppEventService;
import com.storix.domain.domains.event.service.EventContentCacheHelper;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAppEventUseCase {

    private final AppEventService appEventService;
    private final EventContentCacheHelper eventContentCacheHelper;

    // 앱 이벤트 생성
    public CustomResponse<AppEventResponse> createAppEvent(AuthUserDetails authUser, AppEventRequest req) {

        AppEventResponse result = appEventService.create(req.toCommand(), authUser.getUserId());
        return CustomResponse.onSuccess(SuccessCode.ADMIN_APP_EVENT_CREATE_SUCCESS, result);
    }

    // 앱 이벤트 목록 조회
    public CustomResponse<PageResponseWrapperDTO<AppEventResponse>> getAppEvents(int page) {

        PageResponseWrapperDTO<AppEventResponse> result = PageResponseWrapperDTO.from(appEventService.getAppEvents(page));
        return CustomResponse.onSuccess(SuccessCode.ADMIN_APP_EVENT_LOAD_SUCCESS, result);
    }

    // 앱 이벤트 단건 조회
    public CustomResponse<AppEventResponse> getAppEvent(Long appEventId) {

        AppEventResponse result = appEventService.getAppEvent(appEventId);
        return CustomResponse.onSuccess(SuccessCode.ADMIN_APP_EVENT_LOAD_SUCCESS, result);
    }

    // 앱 이벤트 수정
    public CustomResponse<AppEventResponse> updateAppEvent(Long appEventId, AppEventRequest req) {

        // 1. 앱 이벤트 수정
        AppEventResponse result = appEventService.update(appEventId, req.toCommand());

        // 2. 소속 팝업·배너 노출기간도 함께 바뀌므로 캐시 무효화
        eventContentCacheHelper.evict(STORIXStatic.ACTIVE_POPUP_KEY);
        eventContentCacheHelper.evict(STORIXStatic.ACTIVE_BANNER_KEY);
        return CustomResponse.onSuccess(SuccessCode.ADMIN_APP_EVENT_UPDATE_SUCCESS, result);
    }

    // 앱 이벤트 강제 종료
    public CustomResponse<AppEventResponse> cancelAppEvent(Long appEventId) {

        // 1. 앱 이벤트 종료
        AppEventResponse result = appEventService.cancel(appEventId);

        // 2. 소속 팝업·배너도 함께 종료되므로 캐시 무효화
        eventContentCacheHelper.evict(STORIXStatic.ACTIVE_POPUP_KEY);
        eventContentCacheHelper.evict(STORIXStatic.ACTIVE_BANNER_KEY);
        return CustomResponse.onSuccess(SuccessCode.ADMIN_APP_EVENT_CANCEL_SUCCESS, result);
    }
}
