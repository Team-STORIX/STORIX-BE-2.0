package com.storix.api.domain.notification.controller;

import com.storix.api.domain.notification.controller.dto.FcmSendRequest;
import com.storix.api.domain.notification.controller.dto.NotificationDispatchTestRequest;
import com.storix.api.domain.notification.usecase.NotificationSettingUseCase;
import com.storix.api.domain.notification.usecase.NotificationTestUseCase;
import com.storix.api.domain.notification.usecase.NotificationUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.notification.dto.NotificationResponseDto;
import com.storix.domain.domains.notification.dto.NotificationSettingResponse;
import com.storix.domain.domains.notification.dto.UpdateNotificationSettingRequest;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "사용자 알림", description = "알림 관련 API")
public class NotificationController {

    private final NotificationUseCase notificationUseCase;
    private final NotificationSettingUseCase notificationSettingUseCase;
    private final NotificationTestUseCase notificationTestUseCase;

    /** 사용자 알림함 */
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
        return notificationUseCase.getNotifications(authUser.getUserId(), cursorId, size);
    }

    // 2. 안 읽은 알림 개수 조회
    @GetMapping("/unread-count")
    @Operation(summary = "안 읽은 알림 개수 조회", description = "사용자가 아직 읽지 않은 알림의 총 개수를 반환합니다.")
    public CustomResponse<Long> getUnreadCount(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return notificationUseCase.getUnreadCount(authUser.getUserId());
    }

    // 3. 단건 알림 읽음 처리
    @PatchMapping("/{id}")
    @Operation(summary = "단건 알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    public CustomResponse<Void> readNotification(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable("id") Long notificationId
    ) {
        return notificationUseCase.readNotification(authUser.getUserId(), notificationId);
    }

    // 4. 전체 알림 읽음 처리
    @PatchMapping
    @Operation(summary = "전체 알림 읽음 처리", description = "사용자의 모든 안 읽은 알림을 읽음 상태로 일괄 변경합니다.")
    public CustomResponse<Void> readAllNotifications(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return notificationUseCase.readAllNotifications(authUser.getUserId());
    }

    /** 알림 설정 */
    // 5. 알림 설정 조회
    @GetMapping("/settings")
    @Operation(summary = "알림 설정 조회", description = "푸시 알림 수신 여부와 알림 종류별 수신 여부를 조회합니다.")
    public CustomResponse<NotificationSettingResponse> getSettings(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return notificationSettingUseCase.getNotificationSetting(authUser.getUserId());
    }

    // 6. 알림 설정 변경
    @PatchMapping("/settings")
    @Operation(summary = "알림 설정 변경", description = "알림 수신 여부를 갱신합니다. 변경하려는 항목만 requestBody 에 포함하면 됩니다.")
    public CustomResponse<NotificationSettingResponse> updateSettings(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @RequestBody UpdateNotificationSettingRequest request
    ) {
        return notificationSettingUseCase.updateNotificationSetting(authUser.getUserId(), request);
    }


    /** 테스트용 FCM 알림 전송 */
    // [ADMIN][test] FCM 환경변수 세팅 검증
    @PostMapping("/admin/test-push")
    @Operation(summary = "[ADMIN][테스트] FCM 푸시 전송", description = "요청 body 의 token 으로 알림 1건 전송하고 FCM messageId 반환.")
    public CustomResponse<String> sendTestPush(
            @RequestBody @Valid FcmSendRequest request
    ) {
        return notificationTestUseCase.sendTestPush(request);
    }

    // [ADMIN][test] 알림 E2E 검증 (인앱 저장 + 멀티 디바이스 푸시)
    @PostMapping("/admin/test-dispatch")
    @Operation(summary = "[ADMIN][테스트] 알림 E2E (인앱 + 푸시)", description = "지정한 recipientUserId 로 NotificationEvent 를 publish.  \n" +
            "AFTER_COMMIT 후 listener 가 인앱 저장 + 활성 디바이스에 FCM 발송.")
    public CustomResponse<Void> testDispatch(
            @RequestBody @Valid NotificationDispatchTestRequest request
    ) {
        return notificationTestUseCase.testDispatch(request);
    }
}
