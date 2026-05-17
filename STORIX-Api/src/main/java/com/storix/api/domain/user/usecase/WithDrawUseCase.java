package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.service.AuthService;
import com.storix.api.domain.user.helper.OAuthHelper;
import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@UseCase
@RequiredArgsConstructor
public class WithDrawUseCase {

    private final AuthService authService;

    private final OAuthHelper oauthHelper;
    private final CookieHelper cookieHelper;

    public ResponseEntity<CustomResponse<Void>> execute(Long userId) {

        // 1. OAuth 연결 해제
        // TODO: 현재 외부 api 호출 실패 시 feignException 터짐 -> 회원 탈퇴 X -> best-effort + 재시도 워커 처리 고려중
        OAuthInfo oauthInfo = authService.findOAuthInfoByUserId(userId);
        switch (oauthInfo.getProvider()) {
            case KAKAO -> oauthHelper.unlinkKakaoUser(oauthInfo.getOid());
            case NAVER -> oauthHelper.unlinkNaverUser(oauthInfo.getOid());
            case SLACK -> {} // admin 만 해당 (연결 해제 불필요)
            // TODO: Apple 은 refresh_token 저장 후 unlinkAppleUser 호출 필요
            case APPLE -> {}
        }

        // 2. 유저 탈퇴 처리 (RefreshToken / 관심작품 / 서재 삭제 + 푸시 알림 발송 대상 제외)
        authService.withDrawUser(userId);

        return ResponseEntity.ok()
                .headers(cookieHelper.deleteCookie())
                .body(CustomResponse.onSuccess(SuccessCode.AUTH_WITHDRAW_SUCCESS));
    }
}
