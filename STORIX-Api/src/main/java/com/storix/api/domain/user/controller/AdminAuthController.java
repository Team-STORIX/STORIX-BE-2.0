package com.storix.api.domain.user.controller;

import com.storix.api.domain.user.controller.dto.AdminSignupPendingResponse;
import com.storix.api.domain.user.controller.dto.AuthorizationResponse;
import com.storix.api.domain.user.usecase.AdminAuthUseCase;
import com.storix.api.domain.user.usecase.SlackCallbackUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.dto.AdminLoginRequest;
import com.storix.domain.domains.user.dto.AdminSignupRequest;
import com.storix.infrastructure.external.slack.SlackInteractionDto;
import com.storix.infrastructure.external.slack.SlackSignatureVerifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth/admin")
@RequiredArgsConstructor
@Tag(name = "관리자 인증", description = "관리자용 인증 API")
public class AdminAuthController {

    private final AdminAuthUseCase adminAuthUseCase;
    private final SlackCallbackUseCase slackCallbackUseCase;
    private final SlackSignatureVerifier slackSignatureVerifier;

    @Operation(summary = "관리자 회원가입 요청", description = "관리자 회원가입을 요청합니다. Slack 채널에 승인 요청이 전송됩니다.")
    @PostMapping("/signup")
    public ResponseEntity<CustomResponse<AdminSignupPendingResponse>> adminSignup(
            @Valid @RequestBody AdminSignupRequest req
    ) {
        return adminAuthUseCase.adminSignup(req);
    }

    @Operation(summary = "관리자 로그인", description = "Slack 승인 완료 후 email(ID)과 password로 로그인합니다.    \n" +
            "액세스 토큰과 리프레쉬 토큰 쿠키를 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<CustomResponse<AuthorizationResponse>> adminLogin(
            @Valid @RequestBody AdminLoginRequest req
    ) {
        return adminAuthUseCase.adminLogin(req);
    }

    @Operation(hidden = true)
    @PostMapping("/slack/callback")
    public ResponseEntity<String> slackInteractionCallback(HttpServletRequest request) {
        try {
            SlackInteractionDto interaction = slackSignatureVerifier.verify(request);
            slackCallbackUseCase.processAsync(
                    interaction.actionId(), interaction.pendingId(), interaction.responseUrl());
            return ResponseEntity.ok("");
        } catch (IOException e) {
            return ResponseEntity.ok("{\"replace_original\":\"true\",\"text\":\":warning: 처리 중 오류가 발생했습니다.\"}");
        }
    }
}
