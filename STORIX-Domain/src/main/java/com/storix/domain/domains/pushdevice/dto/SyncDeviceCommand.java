package com.storix.domain.domains.pushdevice.dto;

import com.storix.domain.domains.pushdevice.domain.OSPlatform;

public record SyncDeviceCommand(
        String installationId,
        String fcmToken,
        OSPlatform osPlatform,
        String appVersion,
        String osVersion,
        String deviceModel
) {
}
