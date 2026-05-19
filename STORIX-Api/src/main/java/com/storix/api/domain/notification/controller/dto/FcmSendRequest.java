package com.storix.api.domain.notification.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FcmSendRequest(
        @NotBlank(message = "FCM device token 을 보내주세요.")
        @Size(max = 512)
        String token,

        @NotBlank(message = "알림 제목을 보내주세요.")
        @Size(max = 50)
        String title,

        @NotBlank(message = "알림 본문을 보내주세요.")
        @Size(max = 200)
        String body
) {
}
