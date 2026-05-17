package com.storix.api.domain.pushdevice.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// FCM 토큰 갱신 (WorkManager 주기 갱신 / onTokenRefresh) 전용 요청. 디바이스 메타는 보존.
public record RefreshFcmTokenRequest(
        @NotBlank(message = "installationId 는 필수입니다.")
        @Size(max = 64)
        String installationId,

        @NotBlank(message = "fcmToken 은 필수입니다.")
        @Size(max = 512)
        String fcmToken
) {
}
