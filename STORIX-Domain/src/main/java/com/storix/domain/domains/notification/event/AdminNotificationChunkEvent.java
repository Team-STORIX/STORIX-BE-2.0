package com.storix.domain.domains.notification.event;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.notification.domain.AdminNotificationType;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetType;
import com.storix.domain.domains.notification.dto.AdminNotificationBroadcastInfo;

import java.util.List;

public record AdminNotificationChunkEvent(
        Long adminNotificationId,
        String title,
        String content,
        AdminNotificationType notificationType,
        AdminNotificationTargetType targetType,
        Long eventTargetId,
        String targetLink,
        List<Long> userIds
) {

    // 마케팅이면 (광고)/(수신거부) 표기를 인앱·푸시 모두에 적용
    public static AdminNotificationChunkEvent of(Long adminNotificationId, AdminNotificationBroadcastInfo info, List<Long> userIds) {
        boolean marketing = info.notificationType() == AdminNotificationType.MARKETING;
        return new AdminNotificationChunkEvent(
                adminNotificationId,
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
