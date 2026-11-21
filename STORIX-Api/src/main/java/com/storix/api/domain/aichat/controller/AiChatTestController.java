package com.storix.api.domain.aichat.controller;

import com.storix.api.domain.aichat.usecase.AiChatTestUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/chat/tester")
@RequiredArgsConstructor
@Profile({"local", "dev"})
@Tag(name = "AI 작품 검색", description = "AI 작품 검색 API")
public class AiChatTestController {

    private final AiChatTestUseCase aiChatTestUseCase;

    @PostMapping("/reset")
    @Operation(summary = "[테스터] 처음 상태로 되돌리기", description = "내 대화 기록 · 챗봇 설정을 전부 지웁니다.   \n" +
            "다음 진입부터 온보딩이 다시 나옵니다. 로그인한 본인 데이터만 지웁니다.   \n" +
            "prod 에는 등록되지 않습니다.")
    public CustomResponse<Void> reset(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return aiChatTestUseCase.reset(authUser.getUserId());
    }
}
