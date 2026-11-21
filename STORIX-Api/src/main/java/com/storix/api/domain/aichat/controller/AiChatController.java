package com.storix.api.domain.aichat.controller;

import com.storix.api.domain.aichat.controller.dto.AiChatEntryResponse;
import com.storix.api.domain.aichat.usecase.AiChatEntryUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI 작품 검색", description = "AI 작품 검색 API")
public class AiChatController {

    private final AiChatEntryUseCase aiChatEntryUseCase;

    @GetMapping
    @Operation(summary = "[1] 진입", description = "AI 작품 검색 화면에 들어올 때 호출해주세요.   \n" +
            "대화방은 사용자당 하나라 따로 만들지 않습니다.   \n\n" +
            "**onboarding 이 있으면** steps 를 순서대로 말풍선으로 보여 주세요.   \n" +
            "- 선택지가 있는 단계(ADULT · WORKS_TYPE)는 고른 값을 모아 마지막에 챗봇 설정 저장 API 로 보냅니다. 이때 onboardingVersion 에 onboarding.version 을 그대로 넣어주세요   \n" +
            "- ADULT 단계는 성인 인증된 사용자에게만 내려갑니다. 없으면 19금은 '빼고' 로 저장합니다   \n" +
            "- WORKS_TYPE 에서 ANY(상관없음)를 고르면 작품 유형을 빈 배열로 보냅니다   \n" +
            "- 건너뛰기는 19금 끔 · 작품 유형 빈 배열로 저장합니다   \n\n" +
            "**onboarding 이 null 이면** 지난 대화를 불러와 바로 이어 가면 됩니다.   \n" +
            "exampleQuestions 는 입력창 위 예시 질문입니다.")
    public CustomResponse<AiChatEntryResponse> enter(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return aiChatEntryUseCase.enter(authUser.getUserId());
    }
}
