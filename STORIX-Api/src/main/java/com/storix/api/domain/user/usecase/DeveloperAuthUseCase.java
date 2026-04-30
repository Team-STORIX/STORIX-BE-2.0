package com.storix.api.domain.user.usecase;

import com.storix.api.domain.user.controller.dto.AuthorizationResponse;
import com.storix.api.domain.user.controller.dto.DeveloperSignupPendingResponse;
import com.storix.api.domain.user.controller.dto.LoginWithTokenResponse;
import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.api.domain.user.helper.TokenGenerateHelper;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.DeveloperSignupPending;
import com.storix.domain.domains.user.dto.DeveloperLoginRequest;
import com.storix.domain.domains.user.dto.DeveloperSignupRequest;
import com.storix.domain.domains.user.service.DeveloperAuthService;
import com.storix.domain.domains.user.exception.developer.DeveloperIdentifierMismatchException;
import com.storix.infrastructure.external.slack.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

@UseCase
@RequiredArgsConstructor
public class DeveloperAuthUseCase {

    private final DeveloperAuthService developerAuthService;
    private final SlackNotificationService slackNotificationService;
    private final TokenGenerateHelper tokenGenerateHelper;
    private final CookieHelper cookieHelper;

    @Value("${swagger.password}")
    private String swaggerPassword;

    // 개발자 회원가입 요청 → Slack 승인 대기
    public ResponseEntity<CustomResponse<DeveloperSignupPendingResponse>> developerSignup(DeveloperSignupRequest req) {
        if (!swaggerPassword.equals(req.developerIdentifier())) {
            throw DeveloperIdentifierMismatchException.EXCEPTION;
        }

        DeveloperSignupPending pending = developerAuthService.requestSignup(
                req.nickName(), req.favoriteGenreList(), req.favoriteWorksIdList());

        slackNotificationService.sendDeveloperSignupApproval(pending.getPendingId(), pending.getNickName());

        DeveloperSignupPendingResponse response = new DeveloperSignupPendingResponse(pending.getPendingId());

        return ResponseEntity.ok()
                .body(CustomResponse.onSuccess(SuccessCode.DEVELOPER_SIGNUP_PENDING_SUCCESS, response));
    }

    // Slack 승인 콜백 처리
    public AuthUserDetails approveSignup(String pendingId) {
        return developerAuthService.approveDeveloperSignup(pendingId);
    }

    // 개발자 로그인 (pendingId = oid)
    public ResponseEntity<CustomResponse<AuthorizationResponse>> developerLogin(DeveloperLoginRequest req) {
        AuthUserDetails userDetails = developerAuthService.loginDeveloper(req.pendingId());
        LoginWithTokenResponse tokenResponse = tokenGenerateHelper.generateLoginWithToken(userDetails);
        AuthorizationResponse result = AuthorizationResponse.webRefresh(tokenResponse.accessToken());

        return ResponseEntity.ok()
                .headers(cookieHelper.getTokenCookie(tokenResponse.refreshToken()))
                .body(CustomResponse.onSuccess(SuccessCode.DEVELOPER_LOGIN_SUCCESS, result));
    }
}
