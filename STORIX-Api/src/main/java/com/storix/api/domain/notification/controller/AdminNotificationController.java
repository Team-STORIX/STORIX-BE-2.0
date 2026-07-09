package com.storix.api.domain.notification.controller;

import com.storix.api.domain.notification.usecase.AdminNotificationUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.api.domain.notification.controller.dto.AdminNotificationRequest;
import com.storix.api.domain.notification.controller.dto.AdminNotificationResponse;
import com.storix.api.domain.notification.controller.dto.AdminNotificationSummaryResponse;
import com.storix.common.payload.PageResponseWrapperDTO;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "관리자 알림", description = "운영자 알림 관리 API")
public class AdminNotificationController {

    private final AdminNotificationUseCase adminNotificationUseCase;

    @PostMapping
    @Operation(summary = "운영자 알림 생성", description = "즉시 발송이면 생성 후 바로 발송하고, 예약 발송이면 발송 예정 상태로 저장합니다.")
    public CustomResponse<Long> createNotification(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @Valid @RequestBody AdminNotificationRequest req
    ) {
        return adminNotificationUseCase.createNotification(authUser, req);
    }

    @GetMapping
    @Operation(summary = "운영자 알림 목록 조회", description = "최신순 번호형 페이지네이션. 페이지당 10개 고정이며 totalPages/totalElements 를 함께 반환합니다.")
    public CustomResponse<PageResponseWrapperDTO<AdminNotificationSummaryResponse>> getNotifications(
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        return adminNotificationUseCase.getNotifications(page);
    }

    @GetMapping("/{adminNotificationId}")
    @Operation(summary = "운영자 알림 단건 조회")
    public CustomResponse<AdminNotificationResponse> getNotification(
            @PathVariable Long adminNotificationId
    ) {
        return adminNotificationUseCase.getNotification(adminNotificationId);
    }

    @PutMapping("/{adminNotificationId}")
    @Operation(summary = "운영자 알림 수정", description = "발송 예정 상태에서만 수정할 수 있습니다.")
    public CustomResponse<AdminNotificationResponse> updateNotification(
            @PathVariable Long adminNotificationId,
            @Valid @RequestBody AdminNotificationRequest req
    ) {
        return adminNotificationUseCase.updateNotification(adminNotificationId, req);
    }

    @PatchMapping("/{adminNotificationId}/cancel")
    @Operation(summary = "운영자 알림 예약 취소", description = "발송 예정 상태에서만 취소할 수 있습니다.")
    public CustomResponse<AdminNotificationResponse> cancelNotification(
            @PathVariable Long adminNotificationId
    ) {
        return adminNotificationUseCase.cancelNotification(adminNotificationId);
    }

    @PostMapping("/{adminNotificationId}/broadcast")
    @Operation(summary = "운영자 알림 재발송", description = "발송 실패 상태에서만 발송 실패한 유저 대상으로 수동 재발송합니다.")
    public CustomResponse<Void> broadcastNotification(
            @PathVariable Long adminNotificationId
    ) {
        return adminNotificationUseCase.broadcastNotification(adminNotificationId);
    }
}
