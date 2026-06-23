package com.storix.api.domain.user.usecase;

import com.storix.api.domain.user.controller.dto.AdminSignupPendingResponse;
import com.storix.api.domain.user.controller.dto.AuthorizationResponse;
import com.storix.api.domain.user.controller.dto.LoginWithTokenResponse;
import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.api.domain.user.helper.TokenGenerateHelper;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.AdminSignupPending;
import com.storix.domain.domains.user.dto.AdminLoginRequest;
import com.storix.domain.domains.user.dto.AdminSignupRequest;
import com.storix.domain.domains.user.exception.admin.AdminIdentifierMismatchException;
import com.storix.domain.domains.user.service.AdminAuthService;
import com.storix.infrastructure.external.slack.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

@UseCase
@RequiredArgsConstructor
public class AdminAuthUseCase {

    private final AdminAuthService adminAuthService;
    private final SlackNotificationService slackNotificationService;
    
    private final TokenGenerateHelper tokenGenerateHelper;
    private final CookieHelper cookieHelper;

    @Value("${admin.identifier}")
    private String adminIdentifier;

    // 관리자 회원가입 요청
    public ResponseEntity<CustomResponse<AdminSignupPendingResponse>> adminSignup(AdminSignupRequest req) {
        if (!adminIdentifier.equals(req.adminIdentifier())) {
            throw AdminIdentifierMismatchException.EXCEPTION;
        }

        AdminSignupPending pending = adminAuthService.requestSignup(
                req.email(), req.password(), req.nickName());

        slackNotificationService.sendAdminSignupApproval(pending.getPendingId(), pending.getNickName(), pending.getEmail());

        AdminSignupPendingResponse response = new AdminSignupPendingResponse(pending.getPendingId());

        return ResponseEntity.ok()
                .body(CustomResponse.onSuccess(SuccessCode.ADMIN_SIGNUP_PENDING_SUCCESS, response));
    }

    // Slack 승인 콜백 처리
    public AuthUserDetails approveSignup(String pendingId) {
        return adminAuthService.approveAdminSignup(pendingId);
    }

    // 관리자 로그인
    public ResponseEntity<CustomResponse<AuthorizationResponse>> adminLogin(AdminLoginRequest req) {
        AuthUserDetails userDetails = adminAuthService.loginAdmin(req.email(), req.password());
        LoginWithTokenResponse tokenResponse = tokenGenerateHelper.generateLoginWithToken(userDetails);
        AuthorizationResponse result = AuthorizationResponse.webRefresh(tokenResponse.accessToken());

        return ResponseEntity.ok()
                .headers(cookieHelper.getTokenCookie(tokenResponse.refreshToken()))
                .body(CustomResponse.onSuccess(SuccessCode.ADMIN_LOGIN_SUCCESS, result));
    }
}
