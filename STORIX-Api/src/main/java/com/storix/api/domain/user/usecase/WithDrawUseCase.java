package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.WithdrawReason;
import com.storix.domain.domains.user.service.AuthService;
import com.storix.api.domain.user.helper.OAuthHelper;
import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Set;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class WithDrawUseCase {

    private final AuthService authService;

    private final OAuthHelper oauthHelper;
    private final CookieHelper cookieHelper;

    public ResponseEntity<CustomResponse<Void>> execute(Long userId, Set<WithdrawReason> reasons, String detail) {

        // 1. OAuth 연결 해제
        OAuthInfo oauthInfo = authService.findOAuthInfoByUserId(userId);
        try {
            switch (oauthInfo.getProvider()) {
                case KAKAO -> oauthHelper.unlinkKakaoUser(oauthInfo.getOid());
                case NAVER -> oauthHelper.unlinkNaverUser(oauthInfo.getOauthRefreshToken());
                case X -> oauthHelper.unlinkXUser(oauthInfo.getOauthRefreshToken());
                case APPLE -> oauthHelper.unlinkAppleUser(oauthInfo.getOauthRefreshToken());
                case SLACK -> {} // admin 만 해당 (연결 해제 불필요)
            }
            log.info(">>> [Withdraw] OAuth 연결 해제 provider={}", oauthInfo.getProvider());
        } catch (Exception e) {
            log.warn(">>> [Withdraw] OAuth 연결 해제 실패 provider={}", oauthInfo.getProvider(), e);
            // TODO: best-effort + 재시도 워커 처리 고려중
        }

        // 2. 유저 탈퇴 처리 (RefreshToken / 관심작품 / 서재 삭제 + 푸시 알림 발송 대상 제외 + 탈퇴 사유 로그)
        authService.withDrawUser(userId, reasons, detail);
        log.info(">>> [Withdraw] 탈퇴 완료 reasons={}", reasons);

        return ResponseEntity.ok()
                .headers(cookieHelper.deleteCookie())
                .body(CustomResponse.onSuccess(SuccessCode.AUTH_WITHDRAW_SUCCESS));
    }
}
