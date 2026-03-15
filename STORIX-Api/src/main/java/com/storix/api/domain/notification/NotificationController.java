package com.storix.api.domain.notification;

import com.storix.domain.domains.notification.dto.NotificationResponseDto;
import com.storix.domain.domains.notification.service.NotificationService;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "사용자 알림", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    // 1. 전체 알림 목록 조회
    @GetMapping
    @Operation(summary = "전체 알림 목록 조회", description = "커서 기반 무한 스크롤입니다. cursorId는 직전 응답의 마지막 알림 ID이며, 첫 요청은 cursorId를 제외합니다.")
    public CustomResponse<Slice<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @Parameter(description = "마지막으로 받은 알림의 ID (첫 조회 시 null)")
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @Parameter(description = "한 번에 가져올 개수 (기본 10)")
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Slice<NotificationResponseDto> result = notificationService.getNotifications(authUser.getUserId(), cursorId, PageRequest.of(0, size));
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_LOAD_SUCCESS, result);
    }

    // 2. 안 읽은 알림 개수 조회
    @GetMapping("/unread-count")
    @Operation(summary = "안 읽은 알림 개수 조회", description = "사용자가 아직 읽지 않은 알림의 총 개수를 반환합니다.")
    public CustomResponse<Long> getUnreadCount(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        long count = notificationService.getUnreadCount(authUser.getUserId());
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_COUNT_SUCCESS, count);
    }

    // 3. 단건 알림 읽음 처리
    @PatchMapping("/{id}")
    @Operation(summary = "단건 알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    public CustomResponse<Void> readNotification(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable("id") Long notificationId
    ) {
        notificationService.readNotification(authUser.getUserId(), notificationId);
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_READ_SUCCESS);
    }

    // 4. 전체 알림 읽음 처리
    @PatchMapping
    @Operation(summary = "전체 알림 읽음 처리", description = "사용자의 모든 안 읽은 알림을 읽음 상태로 일괄 변경합니다.")
    public CustomResponse<Void> readAllNotifications(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        notificationService.readAllNotifications(authUser.getUserId());
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_READ_ALL_SUCCESS);
    }
}
