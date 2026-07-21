package com.storix.api.domain.appversion.controller;

import com.storix.api.domain.appversion.controller.dto.AppVersionCheckResponse;
import com.storix.api.domain.appversion.usecase.AppVersionUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.pushdevice.domain.OSPlatform;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app-version")
@RequiredArgsConstructor
@Tag(name = "앱 버전", description = "앱 버전 게이팅 API")
public class AppVersionController {

    private final AppVersionUseCase appVersionUseCase;

    @GetMapping("/check")
    @Operation(summary = "앱 버전 확인", description = "설치된 네이티브 버전을 기준으로 강제/권장 업데이트 여부를 판정합니다. status: LATEST / UPDATE_AVAILABLE / UPDATE_REQUIRED")
    public CustomResponse<AppVersionCheckResponse> check(
            @RequestParam OSPlatform platform,
            @RequestParam String version
    ) {
        return appVersionUseCase.check(platform, version);
    }
}
