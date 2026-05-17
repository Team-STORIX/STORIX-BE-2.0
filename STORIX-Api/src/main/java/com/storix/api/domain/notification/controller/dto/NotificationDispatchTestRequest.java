package com.storix.api.domain.notification.controller.dto;

import com.storix.domain.domains.notification.domain.NotificationType;
import com.storix.domain.domains.notification.domain.TargetType;
import com.storix.domain.domains.notification.event.NotificationEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationDispatchTestRequest(
        @NotNull(message = "수신자 userId 를 보내주세요.")
        Long recipientUserId,

        @NotNull(message = "Schema에 맞게 알림 타입을 보내주세요.")
        NotificationType type,

        @NotNull(message = "Schema에 맞게 타겟 타입을 보내주세요.")
        TargetType targetType,

        Long targetId,

        Long parentTargetId,

        @NotBlank(message = "알림 제목을 보내주세요.")
        @Size(max = 50)
        String title,

        @NotBlank(message = "알림 본문을 보내주세요.")
        @Size(max = 200)
        String content
) {
    public NotificationEvent toEvent() {
        return new NotificationEvent(
                recipientUserId, type, targetType, targetId, parentTargetId, title, content
        );
    }
}
