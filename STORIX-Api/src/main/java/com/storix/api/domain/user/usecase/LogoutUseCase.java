package com.storix.api.domain.user.usecase;

import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

@UseCase
@RequiredArgsConstructor
public class LogoutUseCase {

    private final AuthService authService;

    private final CookieHelper cookieHelper;

    // 로그아웃
    public ResponseEntity<CustomResponse<Void>> execute(Long userId, String installationId, String refreshToken) {

        // refreshToken 삭제 (Redis) 는 트랜잭션 밖에서 끝낸다
        authService.deleteRefreshToken(userId, refreshToken);

        // [Native] 해당 디바이스 FCM 토큰 비활성화
        authService.deactivatePushDevice(userId, installationId);

        return ResponseEntity.ok()
                    .headers(cookieHelper.deleteCookie())
                    .body(CustomResponse.onSuccess(SuccessCode.AUTH_LOGOUT_SUCCESS));
    }

}
