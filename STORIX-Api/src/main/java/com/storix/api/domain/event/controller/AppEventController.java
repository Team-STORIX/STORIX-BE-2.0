package com.storix.api.domain.event.controller;

import com.storix.api.domain.event.usecase.AppEventUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AppEventPageResponse;
import com.storix.domain.domains.event.dto.BannerModalResponse;
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

    @GetMapping("/{appEventId}")
    @Operation(
            summary = "앱 이벤트 상세 조회 (인증 불필요)",
            description = """
                    이벤트 상세 웹페이지(storix.kr/event/{appEventId})를 그리는 데 필요한 정보를 반환합니다.
                    로그인하지 않아도 호출할 수 있으며, eventType 으로 어떤 화면을 그릴지 정하시면 됩니다.

                    운영 설정값(홍보 수단, 응모권 지급 기준 등)은 내려가지 않습니다.
                    존재하지 않는 이벤트, 그리고 아직 시작하지 않은 이벤트는 404입니다. (오픈 전 내용이 미리 노출되지 않도록)
                    종료된 이벤트는 조회되며 status=ENDED 로 내려갑니다.
                    """
    )
    public CustomResponse<AppEventPageResponse> getAppEvent(
            @PathVariable Long appEventId
    ) {
        return appEventUseCase.getAppEventPage(appEventId);
    }

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

    @PatchMapping("/popup/{popupId}/never-show")
    @Operation(summary = "팝업 다시 보지 않기", description = "해당 팝업을 노출 기간 내내 이 유저에게 다시 노출하지 않습니다.")
    public CustomResponse<Void> neverShowPopup(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable Long popupId
    ) {
        return appEventUseCase.dismissPopupForever(authUser.getUserId(), popupId);
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

    @GetMapping("/banner/{bannerId}/modal-required")
    @Operation(summary = "배너 최초 안내 모달 필요 여부 조회", description = "해당 배너를 한 번도 확인하지 않은 유저인지 반환합니다. true면 최초 안내 모달을 띄우세요. 배너 종류 무관 범용 API입니다. 존재하지 않는 배너는 404.")
    public CustomResponse<BannerModalResponse> getBannerModalStatus(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable Long bannerId
    ) {
        return appEventUseCase.getBannerModalStatus(authUser.getUserId(), bannerId);
    }

    @PatchMapping("/banner/{bannerId}/confirm")
    @Operation(summary = "배너 확인 처리", description = "프론트가 배너 안내 모달을 실제 표시한 뒤 호출하면 해당 유저에게 다시 필요하다고 내려주지 않습니다. 배너 종류 무관 범용 API입니다. 존재하지 않는 배너는 404.")
    public CustomResponse<Void> confirmBannerModal(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable Long bannerId
    ) {
        return appEventUseCase.confirmBannerModal(authUser.getUserId(), bannerId);
    }
}
