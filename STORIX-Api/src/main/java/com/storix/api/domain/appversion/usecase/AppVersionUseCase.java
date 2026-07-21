package com.storix.api.domain.appversion.usecase;

import com.storix.api.domain.appversion.controller.dto.AppVersionCheckResponse;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.appversion.service.AppVersionService;
import com.storix.domain.domains.pushdevice.domain.OSPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppVersionUseCase {

    private final AppVersionService appVersionService;

    public CustomResponse<AppVersionCheckResponse> check(OSPlatform platform, String version) {
        return CustomResponse.onSuccess(
                SuccessCode.APP_VERSION_CHECK_SUCCESS,
                AppVersionCheckResponse.from(appVersionService.check(platform, version)));
    }
}
