package com.storix.api.domain.pushdevice.controller.dto;

import com.storix.domain.domains.pushdevice.domain.OSPlatform;
import com.storix.domain.domains.pushdevice.dto.SyncDeviceCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeviceSyncRequest(
        // 1. FCM 전송을 위해 필요한 정보
        @NotBlank(message = "기기 식별을 위한 uuid를 생성하여 storage 저장 후 보내주세요.")
        @Size(max = 64)
        String installationId,

        @NotBlank(message = "fcm 토큰을 보내주세요.")
        @Size(max = 512)
        String fcmToken,

        @NotNull(message = "osPlatform(IOS / ANDROID) 정보를 보내주세요.")
        OSPlatform osPlatform,

        // 2. FCM 모니터링을 위해 필요한 정보
        @NotBlank(message = "설치된 앱 버전을 보내주세요.")
        @Size(max = 32)
        String appVersion,

        @NotBlank(message = "디바이스의 OS 버전 정보를 보내주세요.")
        @Size(max = 32)
        String osVersion,

        @NotBlank(message = "디바이스의 모델명 정보를 보내주세요.")
        @Size(max = 64)
        String deviceModel
) {
    public SyncDeviceCommand toCommand() {
        return new SyncDeviceCommand(installationId, fcmToken, osPlatform, appVersion, osVersion, deviceModel);
    }
}
