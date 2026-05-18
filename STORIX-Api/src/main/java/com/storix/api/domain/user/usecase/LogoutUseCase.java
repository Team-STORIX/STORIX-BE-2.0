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
    public ResponseEntity<CustomResponse<Void>> execute(Long userId, String installationId) {

        // refreshToken 삭제 + [Native] 해당 디바이스 FCM 토큰 비활성화
        authService.logout(userId, installationId);

        return ResponseEntity.ok()
                    .body(CustomResponse.onSuccess(SuccessCode.AUTH_LOGOUT_SUCCESS));
    }
}
