package com.storix.api.domain.user.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.api.domain.user.controller.dto.AuthorizationResponse;
import com.storix.api.domain.user.controller.dto.DeveloperSignupPendingResponse;
import com.storix.api.domain.user.usecase.DeveloperAuthUseCase;
import com.storix.api.domain.user.usecase.SlackCallbackProcessor;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.dto.DeveloperLoginRequest;
import com.storix.domain.domains.user.dto.DeveloperSignupRequest;
import com.storix.infrastructure.external.slack.SlackSignatureVerifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth/developer")
@RequiredArgsConstructor
@Tag(name = "개발자", description = "개발자용 인증 API")
public class DeveloperAuthController {

    private final DeveloperAuthUseCase developerAuthUseCase;
    private final SlackCallbackProcessor slackCallbackProcessor;
    private final SlackSignatureVerifier slackSignatureVerifier;
    private final ObjectMapper objectMapper;

    @Operation(summary = "개발자 회원가입 요청", description = "개발자 회원가입을 요청합니다. Slack 채널에 승인 요청이 전송됩니다.\n반환된 pendingId로 로그인 API를 호출하세요.")
    @PostMapping("/signup")
    public ResponseEntity<CustomResponse<DeveloperSignupPendingResponse>> developerSignup(
            @Valid @RequestBody DeveloperSignupRequest req
    ) {
        return developerAuthUseCase.developerSignup(req);
    }

    @Operation(summary = "개발자 로그인", description = "Slack 승인 완료 후 pendingId로 로그인합니다.\n액세스 토큰과 리프레쉬 토큰 쿠키를 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<CustomResponse<AuthorizationResponse>> developerLogin(
            @Valid @RequestBody DeveloperLoginRequest req
    ) {
        return developerAuthUseCase.developerLogin(req);
    }

    @Operation(hidden = true)
    @PostMapping("/slack/callback")
    public ResponseEntity<String> slackInteractionCallback(HttpServletRequest request) {
        try {
            String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String timestamp = request.getHeader("X-Slack-Request-Timestamp");
            String signature = request.getHeader("X-Slack-Signature");

            slackSignatureVerifier.verify(timestamp, rawBody, signature);

            String payload = URLDecoder.decode(
                    rawBody.substring("payload=".length()), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(payload);
            String actionId = root.at("/actions/0/action_id").asText();
            String pendingId = root.at("/actions/0/value").asText();
            String responseUrl = root.at("/response_url").asText();

            slackCallbackProcessor.processAsync(actionId, pendingId, responseUrl);
        } catch (IOException e) {
            log.error(">>>> [Slack] 콜백 파싱 실패", e);
            return ResponseEntity.ok("{\"replace_original\":\"true\",\"text\":\":warning: 처리 중 오류가 발생했습니다.\"}");
        }
        return ResponseEntity.ok("");
    }
}
