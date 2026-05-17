package com.storix.api.domain.pushdevice.controller;

import com.storix.api.domain.pushdevice.controller.dto.DeviceSyncRequest;
import com.storix.api.domain.pushdevice.controller.dto.RefreshFcmTokenRequest;
import com.storix.api.domain.pushdevice.usecase.PushDeviceUseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "푸시 알림 디바이스", description = "푸시 알림 발송용 디바이스 토큰 등록/해제 API")
public class PushDeviceController {

    private final PushDeviceUseCase pushDeviceUseCase;

    @PostMapping("/sync")
    @Operation(summary = "단일 디바이스 푸시 알림 상태 동기화", description = "단일 디바이스 정보와 FCM 토큰 정보를 BE 서버에 동기화합니다.   \n" +
            "1) 앱 foreground 진입 후 토큰 재발급을 통한 자동로그인 / 회원가입 / 로그인 시 호출해주세요.   \n" +
            "2) OS 푸시 알림 권한이 OFF -> ON 으로 변경될 떄 호출해주세요.   \n" +
            "3) 디바이스 관련 메타 정보(OS 버전 / 앱 버전)가 변경될 때 호출해주세요.")
    public CustomResponse<Void> sync(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @RequestBody @Valid DeviceSyncRequest request
    ) {
        pushDeviceUseCase.sync(authUser.getUserId(), request);
        return CustomResponse.onSuccess(SuccessCode.DEVICE_SYNC_SUCCESS);
    }

    @DeleteMapping("/{installationId}")
    @Operation(summary = "단일 디바이스 푸시 알림 해제", description = "단일 디바이스에 할당된 FCM 토큰을 BE 서버에서 비활성화합니다.   \n" +
            "1) OS 푸시 알림 권한이 ON -> OFF 로 변경될 때 호출해주세요.")
    public CustomResponse<Void> unregister(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable String installationId
    ) {
        pushDeviceUseCase.unregister(authUser.getUserId(), installationId);
        return CustomResponse.onSuccess(SuccessCode.DEVICE_UNREGISTER_SUCCESS);
    }

    @PatchMapping("/token")
    @Operation(summary = "FCM 토큰 갱신", description = "FCM 토큰 정보만 갱신하는 api 입니다. 단일 디바이스 푸시 알림 등록(/sync)이 선행되어야 합니다.  \n" +
            "1) FCM onTokenRefresh 콜백 시 호출해주세요. (앱 재설치 / 데이터 초기화 등)  \n" +
            "2) WorkManager 주기(30일) 호출 시 직전 캐시 토큰과 비교해 갱신된 경우에만 호출해주세요.")
    public CustomResponse<Void> refreshFcmToken(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @RequestBody @Valid RefreshFcmTokenRequest request
    ) {
        pushDeviceUseCase.refreshFcmToken(authUser.getUserId(), request);
        return CustomResponse.onSuccess(SuccessCode.DEVICE_TOKEN_REFRESH_SUCCESS);
    }
}
