package com.storix.api.domain.event.controller;

import com.storix.api.domain.event.controller.dto.AppEventRequest;
import com.storix.api.domain.event.controller.dto.AppEventWinnerDrawRequest;
import com.storix.common.payload.PageResponseWrapperDTO;
import com.storix.api.domain.event.usecase.AdminAppEventUseCase;
import com.storix.api.domain.event.usecase.AppEventWinnerUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AppEventResponse;
import com.storix.domain.domains.event.dto.AppEventWinnerDrawResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/app-events")
@RequiredArgsConstructor
@Validated
@Tag(name = "관리자 앱 이벤트", description = "관리자 앱 이벤트 관리 API")
public class AdminAppEventController {

    private final AdminAppEventUseCase adminAppEventUseCase;
    private final AppEventWinnerUseCase appEventWinnerUseCase;

    @PostMapping
    @Operation(summary = "앱 이벤트 생성", description = "생성된 앱 이벤트의 appEventId 를 반환합니다. 팝업/배너/알림이 이 id 로 이벤트에 묶입니다.")
    public CustomResponse<AppEventResponse> createAppEvent(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @Valid @RequestBody AppEventRequest req
    ) {
        return adminAppEventUseCase.createAppEvent(authUser, req);
    }

    @GetMapping
    @Operation(summary = "앱 이벤트 목록 조회", description = "최신순 번호형 페이지네이션. 페이지당 10개 고정이며 totalPages/totalElements 를 함께 반환합니다. 검색 시 keyword로 이벤트명을 보내주세요.")
    public CustomResponse<PageResponseWrapperDTO<AppEventResponse>> getAppEvents(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) String keyword
    ) {
        return adminAppEventUseCase.getAppEvents(page, keyword);
    }

    @GetMapping("/{appEventId}")
    @Operation(summary = "앱 이벤트 단건 조회")
    public CustomResponse<AppEventResponse> getAppEvent(
            @PathVariable Long appEventId
    ) {
        return adminAppEventUseCase.getAppEvent(appEventId);
    }

    @PutMapping("/{appEventId}")
    @Operation(summary = "앱 이벤트 수정")
    public CustomResponse<AppEventResponse> updateAppEvent(
            @PathVariable Long appEventId,
            @Valid @RequestBody AppEventRequest req
    ) {
        return adminAppEventUseCase.updateAppEvent(appEventId, req);
    }

    @PatchMapping("/{appEventId}/cancel")
    @Operation(summary = "앱 이벤트 강제 종료")
    public CustomResponse<AppEventResponse> cancelAppEvent(
            @PathVariable Long appEventId
    ) {
        return adminAppEventUseCase.cancelAppEvent(appEventId);
    }

    @PostMapping("/{appEventId}/winners")
    @Operation(
            summary = "앱 이벤트 당첨자 확정",
            description = """
                    당첨자를 뽑는 이벤트(hasWinner=true)의 당첨자를 확정하고, 뽑힌 순서대로 반환합니다.

                    추첨은 이벤트당 최초 1회만 실행되고 결과가 저장됩니다.
                    이미 확정된 이벤트에 다시 호출하면 재추첨 없이 확정된 당첨자를 반환하며, 이때 alreadyFinalized=true 입니다.
                    확정된 당첨자는 관리자 알림(대상 EVENT_WINNERS) 발송 대상이 됩니다.

                    이벤트 진행 중에도 호출할 수 있으나 (조기 종료 시), 확정 이후의 참여는 반영되지 않습니다.
                    출석 이벤트의 응모권 통계까지 함께 보려면 GET /api/v1/admin/attendance-events/{appEventId}/winners 를 호출해주세요!

                    존재하지 않는 이벤트면 404, 당첨자를 뽑지 않는 이벤트(hasWinner=false)면 400,
                    당첨자 확정 로직이 구현되지 않은 이벤트 종류면 500.
                    """
    )
    public CustomResponse<AppEventWinnerDrawResponse> drawWinners(
            @PathVariable Long appEventId,
            @Valid @RequestBody AppEventWinnerDrawRequest req
    ) {
        return appEventWinnerUseCase.draw(appEventId, req);
    }
}
