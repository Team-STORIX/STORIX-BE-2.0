package com.storix.api.domain.notification.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.notification.dto.NotificationResponseDto;
import com.storix.domain.domains.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

@UseCase
@RequiredArgsConstructor
public class NotificationUseCase {

    private final NotificationService notificationService;

    // 전체 알림 목록 조회
    public CustomResponse<Slice<NotificationResponseDto>> getNotifications(Long userId, Long cursorId, int size) {
        Slice<NotificationResponseDto> result = notificationService.getNotifications(userId, cursorId, PageRequest.of(0, size));
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_LOAD_SUCCESS, result);
    }

    // 안 읽은 알림 개수 조회
    public CustomResponse<Long> getUnreadCount(Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_COUNT_SUCCESS, count);
    }

    // 단건 알림 읽음 처리
    public CustomResponse<Void> readNotification(Long userId, Long notificationId) {
        notificationService.readNotification(userId, notificationId);
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_READ_SUCCESS);
    }

    // 전체 알림 읽음 처리
    public CustomResponse<Void> readAllNotifications(Long userId) {
        notificationService.readAllNotifications(userId);
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_READ_ALL_SUCCESS);
    }
}
