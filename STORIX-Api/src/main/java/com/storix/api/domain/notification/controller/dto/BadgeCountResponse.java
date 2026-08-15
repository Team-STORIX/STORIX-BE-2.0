package com.storix.api.domain.notification.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BadgeCountResponse(

        @Schema(description = "앱 아이콘에 표시할 배지 숫자. 알림함 미읽음 + 토픽룸 미읽음, 상한 없음", example = "23")
        long badgeCount
) {
}
