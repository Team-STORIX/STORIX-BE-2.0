package com.storix.api.domain.event.controller;

import com.storix.api.domain.event.controller.dto.AppEventRequest;
import com.storix.api.domain.event.controller.dto.PageResponse;
import com.storix.api.domain.event.usecase.AdminAppEventUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AppEventResponse;
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

    @PostMapping
    @Operation(summary = "앱 이벤트 생성", description = "생성된 앱 이벤트의 appEventId 를 반환합니다. 팝업/배너/알림이 이 id 로 이벤트에 묶입니다.")
    public CustomResponse<AppEventResponse> createAppEvent(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @Valid @RequestBody AppEventRequest req
    ) {
        return adminAppEventUseCase.createAppEvent(authUser, req);
    }

    @GetMapping
    @Operation(summary = "앱 이벤트 목록 조회", description = "최신순 번호형 페이지네이션. 페이지당 10개 고정이며 totalPages/totalElements 를 함께 반환합니다.")
    public CustomResponse<PageResponse<AppEventResponse>> getAppEvents(
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        return adminAppEventUseCase.getAppEvents(page);
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
}
