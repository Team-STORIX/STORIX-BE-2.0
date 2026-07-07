package com.storix.api.domain.event.controller;

import com.storix.api.domain.event.usecase.AppEventUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.BannerResponse;
import com.storix.domain.domains.event.dto.OneTimeAppEventResponse;
import com.storix.domain.domains.event.dto.PopupResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/app-events")
@RequiredArgsConstructor
@Tag(name = "앱 이벤트", description = "앱 이벤트 (팝업 모달 / 배너) API")
public class AppEventController {

    private final AppEventUseCase appEventUseCase;

    @GetMapping("/popup")
    @Operation(summary = "노출 중인 팝업 조회", description = "현재 노출 가능한 팝업을 반환합니다. 유저가 오늘 '다시 안 보기' 했거나 노출 팝업이 없으면 null.")
    public CustomResponse<PopupResponse> getPopup(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return appEventUseCase.getActivePopup(authUser.getUserId());
    }

    @PatchMapping("/popup/{popupId}/dismiss")
    @Operation(summary = "팝업 오늘 다시 안 보기", description = "해당 팝업을 오늘 하루 이 유저에게 다시 노출하지 않습니다. (다음 날 다시 노출)")
    public CustomResponse<Void> dismissPopup(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable Long popupId
    ) {
        return appEventUseCase.dismissPopupForToday(authUser.getUserId(), popupId);
    }

    @GetMapping("/banner")
    @Operation(summary = "노출 중인 배너 조회", description = "현재 노출 가능한 배너를 최대 3개까지 반환합니다. 없으면 빈 배열.")
    public CustomResponse<List<BannerResponse>> getBanner() {
        return appEventUseCase.getActiveBanner();
    }

    @GetMapping("/title")
    @Operation(summary = "칭호 획득 조회", description = "유저에게 아직 표시하지 않은 칭호 획득 이벤트를 반환합니다.")
    public CustomResponse<List<OneTimeAppEventResponse>> getAcquiredTitleEvents(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return appEventUseCase.getPendingAcquiredTitleEvents(authUser.getUserId());
    }

    @PatchMapping("/title/{eventId}/ack")
    @Operation(summary = "칭호 획득 확인 처리", description = "프론트가 칭호 획득을 실제 표시한 뒤 호출하면 해당 이벤트를 다시 내려주지 않습니다.")
    public CustomResponse<Void> ackAcquiredTitleEvent(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable Long eventId
    ) {
        return appEventUseCase.ackAcquiredTitleEvent(authUser.getUserId(), eventId);
    }
}
