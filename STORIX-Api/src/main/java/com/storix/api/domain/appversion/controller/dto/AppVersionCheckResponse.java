package com.storix.api.domain.appversion.controller.dto;

import com.storix.domain.domains.appversion.domain.VersionStatus;
import com.storix.domain.domains.appversion.dto.AppVersionCheck;
import io.swagger.v3.oas.annotations.media.Schema;

public record AppVersionCheckResponse(

        @Schema(description = "버전 판정 상태", example = "UPDATE_REQUIRED")
        VersionStatus status,

        @Schema(description = "최신 스토어 버전", example = "1.0.0")
        String latestVersion,

        @Schema(description = "최소 지원 버전 (미만이면 강제 업데이트)", example = "1.0.0")
        String minSupportedVersion
) {
    public static AppVersionCheckResponse from(AppVersionCheck check) {
        return new AppVersionCheckResponse(
                check.status(),
                check.latestVersion(),
                check.minSupportedVersion()
        );
    }
}
