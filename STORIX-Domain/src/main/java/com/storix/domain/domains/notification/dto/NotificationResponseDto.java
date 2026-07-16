package com.storix.domain.domains.notification.dto;

import com.storix.domain.domains.notification.domain.Notification;
import com.storix.domain.domains.notification.domain.NotificationCategory;
import com.storix.domain.domains.notification.domain.NotificationType;
import com.storix.domain.domains.notification.domain.TargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponseDto {

    private Long id;
    private NotificationType notificationType;
    private NotificationCategory category;
    private TargetType targetType;
    private Long targetId;
    private Long parentTargetId;
    private String targetLink;
    private String title;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType())
                .category(notification.getNotificationType().category()) // 알림 분류 라벨 (UI 아이콘 / 그룹)
                .targetType(notification.getTargetType()) // 알림 클릭 시 라우팅 대상 (페이지 이동)
                .targetId(notification.getTargetId())
                .parentTargetId(notification.getParentTargetId())
                .targetLink(notification.getTargetLink())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
