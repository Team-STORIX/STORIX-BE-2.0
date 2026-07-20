package com.storix.api.domain.notification.usecase;

import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.common.payload.PageResponseWrapperDTO;
import com.storix.domain.domains.notification.domain.AdminNotificationSendType;
import com.storix.api.domain.notification.controller.dto.AdminNotificationRequest;
import com.storix.api.domain.notification.controller.dto.AdminNotificationResponse;
import com.storix.api.domain.notification.controller.dto.AdminNotificationSummaryResponse;
import com.storix.domain.domains.notification.service.AdminNotificationService;
import com.storix.domain.domains.notification.service.AdminNotificationLifecycleService;
import com.storix.domain.domains.notification.service.AdminNotificationBroadcastService;
import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminNotificationUseCase {

    private final AdminNotificationService adminNotificationService;
    private final AdminNotificationLifecycleService adminNotificationLifecycleService;
    private final AdminNotificationBroadcastService adminNotificationBroadcastService;
    private final AppEventAdaptor appEventAdaptor;

    // 운영자 알림 생성
    public CustomResponse<Long> createNotification(AuthUserDetails authUser, AdminNotificationRequest req) {

        // 0. 이벤트를 참조하면 대상 이벤트 존재 재확인
        if (req.eventTargetId() != null) {
            appEventAdaptor.findById(req.eventTargetId());
        }

        // 1. 운영자 알림 생성 (생성 관리자 id 저장)
        Long adminNotificationId = adminNotificationService.create(req.toCommand(), authUser.getUserId());

        // 2. 즉시 발송인 경우, 브로드캐스트 발송
        if (req.sendType() == AdminNotificationSendType.IMMEDIATE) {
            adminNotificationBroadcastService.broadcast(adminNotificationId);
        }

        return CustomResponse.onSuccess(SuccessCode.ADMIN_NOTIFICATION_CREATE_SUCCESS, adminNotificationId);
    }

    // 운영자 알림 목록 조회
    public CustomResponse<PageResponseWrapperDTO<AdminNotificationSummaryResponse>> getNotifications(int page, String keyword) {

        PageResponseWrapperDTO<AdminNotificationSummaryResponse> result = PageResponseWrapperDTO.from(
                adminNotificationService.getNotifications(page, keyword).map(AdminNotificationSummaryResponse::from));
        return CustomResponse.onSuccess(SuccessCode.ADMIN_NOTIFICATION_LOAD_SUCCESS, result);
    }

    // 운영자 알림 단건 조회
    public CustomResponse<AdminNotificationResponse> getNotification(Long adminNotificationId) {

        AdminNotificationResponse result =
                AdminNotificationResponse.from(adminNotificationService.getById(adminNotificationId));
        return CustomResponse.onSuccess(SuccessCode.ADMIN_NOTIFICATION_LOAD_SUCCESS, result);
    }

    // 운영자 알림 수정
    public CustomResponse<AdminNotificationResponse> updateNotification(Long adminNotificationId, AdminNotificationRequest req) {

        // 0. 이벤트를 참조하면(eventTargetId) 대상 이벤트 존재 재확인 (없으면 AppEventNotFoundException)
        if (req.eventTargetId() != null) {
            appEventAdaptor.findById(req.eventTargetId());
        }

        // 1. 운영자 알림 수정
        AdminNotificationResponse result =
                AdminNotificationResponse.from(adminNotificationService.update(adminNotificationId, req.toCommand()));

        // 2. 즉시 발송으로 변경된 경우, 브로드캐스트 발송
        if (req.sendType() == AdminNotificationSendType.IMMEDIATE) {
            adminNotificationBroadcastService.broadcast(adminNotificationId);
        }

        return CustomResponse.onSuccess(SuccessCode.ADMIN_NOTIFICATION_UPDATE_SUCCESS, result);
    }

    // 운영자 알림 예약 취소
    public CustomResponse<AdminNotificationResponse> cancelNotification(Long adminNotificationId) {

        AdminNotificationResponse result =
                AdminNotificationResponse.from(adminNotificationService.cancel(adminNotificationId));
        return CustomResponse.onSuccess(SuccessCode.ADMIN_NOTIFICATION_CANCEL_SUCCESS, result);
    }

    // 운영자 알림 수동 재발송
    public CustomResponse<Void> broadcastNotification(Long adminNotificationId) {
        adminNotificationLifecycleService.prepareRebroadcast(adminNotificationId);
        return CustomResponse.onSuccess(SuccessCode.ADMIN_NOTIFICATION_BROADCAST_SUCCESS);
    }
}
