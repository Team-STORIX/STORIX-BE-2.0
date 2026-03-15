package com.storix.storix_api.domains.notification.dto;

import com.storix.storix_api.domains.notification.domain.Notification;
import com.storix.storix_api.domains.notification.domain.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponseDto {

    private Long id;
    private NotificationType notificationType;
    private String content;
    private Long targetId;
    private boolean isRead;
    private LocalDateTime createdAt;

    // Entity -> DTO 변환 정적 팩토리 메서드
    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType())
                .content(notification.getContent())
                .targetId(notification.getTargetId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt()) // BaseEntity의 getter
                .build();
    }
}
