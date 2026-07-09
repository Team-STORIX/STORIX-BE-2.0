package com.storix.domain.domains.notification.event;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.notification.domain.AdminNotificationType;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetType;
import com.storix.domain.domains.notification.dto.AdminNotificationBroadcastInfo;

import java.util.List;

public record AdminNotificationChunkEvent(
        Long adminNotificationId,
        String title,          // 인앱 알림함 — 원문
        String content,        // 인앱 알림함 — 원문
        String pushTitle,      // 푸시 — 마케팅이면 (광고) 표기
        String pushContent,    // 푸시 — 마케팅이면 (수신거부) 표기
        AdminNotificationType notificationType,
        AdminNotificationTargetType targetType,
        Long eventTargetId,
        String targetLink,
        List<Long> userIds
) {

    // 인앱은 원문, 푸시는 마케팅일 때 (광고)/(수신거부) 표기
    public static AdminNotificationChunkEvent of(Long adminNotificationId, AdminNotificationBroadcastInfo info, List<Long> userIds) {
        boolean marketing = info.notificationType() == AdminNotificationType.MARKETING;
        return new AdminNotificationChunkEvent(
                adminNotificationId,
                info.title(),
                info.content(),
                marketing ? String.format(STORIXStatic.Notification.TITLE_MARKETING, info.title()) : info.title(),
                marketing ? String.format(STORIXStatic.Notification.TPL_MARKETING, info.content()) : info.content(),
                info.notificationType(),
                info.targetType(),
                info.eventTargetId(),
                info.targetLink(),
                userIds
        );
    }

    public boolean isMarketing() {
        return notificationType == AdminNotificationType.MARKETING;
    }
}
