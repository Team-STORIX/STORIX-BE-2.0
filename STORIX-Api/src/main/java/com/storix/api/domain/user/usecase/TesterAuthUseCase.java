package com.storix.api.domain.user.usecase;

import com.storix.api.domain.user.controller.dto.AuthorizationResponse;
import com.storix.api.domain.user.controller.dto.TesterSignupPendingResponse;
import com.storix.api.domain.user.controller.dto.LoginWithTokenResponse;
import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.api.domain.user.helper.TokenGenerateHelper;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.TesterSignupPending;
import com.storix.domain.domains.user.dto.TesterLoginRequest;
import com.storix.domain.domains.user.dto.TesterSignupRequest;
import com.storix.domain.domains.user.service.TesterAuthService;
import com.storix.domain.domains.user.exception.tester.TesterIdentifierMismatchException;
import com.storix.infrastructure.external.slack.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

@UseCase
@RequiredArgsConstructor
public class TesterAuthUseCase {

    private final TesterAuthService testerAuthService;
    private final SlackNotificationService slackNotificationService;
    private final TokenGenerateHelper tokenGenerateHelper;
    private final CookieHelper cookieHelper;

    @Value("${swagger.password}")
    private String swaggerPassword;

    // 테스터 회원가입 요청 → Slack 승인 대기
    public ResponseEntity<CustomResponse<TesterSignupPendingResponse>> testerSignup(TesterSignupRequest req) {
        if (!swaggerPassword.equals(req.testerIdentifier())) {
            throw TesterIdentifierMismatchException.EXCEPTION;
        }

        TesterSignupPending pending = testerAuthService.requestSignup(
                req.nickName(), req.favoriteGenreList(), req.favoriteWorksIdList());

        slackNotificationService.sendTesterSignupApproval(pending.getPendingId(), pending.getNickName());

        TesterSignupPendingResponse response = new TesterSignupPendingResponse(pending.getPendingId());

        return ResponseEntity.ok()
                .body(CustomResponse.onSuccess(SuccessCode.TESTER_SIGNUP_PENDING_SUCCESS, response));
    }

    // Slack 승인 콜백 처리
    public AuthUserDetails approveSignup(String pendingId) {
        return testerAuthService.approveTesterSignup(pendingId);
    }

    // 테스터 로그인 (pendingId = oid)
    public ResponseEntity<CustomResponse<AuthorizationResponse>> testerLogin(TesterLoginRequest req) {
        AuthUserDetails userDetails = testerAuthService.loginTester(req.pendingId());
        LoginWithTokenResponse tokenResponse = tokenGenerateHelper.generateLoginWithToken(userDetails);
        AuthorizationResponse result = AuthorizationResponse.webRefresh(tokenResponse.accessToken());

        return ResponseEntity.ok()
                .headers(cookieHelper.getTokenCookie(tokenResponse.refreshToken()))
                .body(CustomResponse.onSuccess(SuccessCode.TESTER_LOGIN_SUCCESS, result));
    }
}
