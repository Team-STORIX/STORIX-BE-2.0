package com.storix.internal.aichat.controller;

import com.storix.internal.aichat.controller.dto.AiUserContextResponse;
import com.storix.internal.aichat.controller.dto.AiWorksCatalogResponse;
import com.storix.internal.aichat.usecase.InternalAiUserContextUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// AI 서버만 부른다. 공개 도메인에서는 Caddy 가 막고, storix-net 안에서 X-Internal-Token 으로만 열린다
// 응답은 STORIX-AI 계약(UserContext)을 그대로 따라 CustomResponse 로 감싸지 않는다
@RestController
@RequestMapping("/internal/v1/ai")
@RequiredArgsConstructor
@Tag(name = "내부 AI", description = "AI 서버 전용 내부 API. storix-net 안에서 X-Internal-Token 헤더로만 호출합니다")
public class InternalAiChatController {

    private final InternalAiUserContextUseCase internalAiUserContextUseCase;

    // userId 는 Spring 이 JWT 에서 꺼내 AI 서버에 넘긴 값. AI 가 만든 값을 받지 않는다
    @GetMapping("/users/{userId}/context")
    @Operation(summary = "사용자 맥락", description = "취향 장르 · 해시태그, 서재(리뷰한 작품) · 관심작품 ID")
    public AiUserContextResponse findContext(@PathVariable Long userId) {
        return internalAiUserContextUseCase.find(userId);
    }

    @GetMapping("/works/enum-catalog")
    @Operation(summary = "작품 enum 카탈로그",
            description = "장르 · 연령가 · 작품 유형 · 플랫폼의 enum 이름과 DB 값, 19금으로 보는 연령가, 챗봇 설정에서 고를 수 있는 작품 유형. "
                    + "AI 서버가 조건 추출 · 색인 · 필터에서 enum 값을 맞출 때 씁니다")
    public AiWorksCatalogResponse findWorksCatalog() {
        return AiWorksCatalogResponse.create();
    }
}
