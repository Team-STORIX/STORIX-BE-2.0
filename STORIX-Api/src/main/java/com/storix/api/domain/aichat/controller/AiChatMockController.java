package com.storix.api.domain.aichat.controller;

import com.storix.api.domain.aichat.controller.dto.AiChatMessageRequest;
import com.storix.api.domain.aichat.usecase.AiChatMockUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Profile("!prod")
@RestController
@RequestMapping("/api/v1/ai/chats")
@RequiredArgsConstructor
@Tag(name = "AI 작품 검색", description = "AI 작품 검색 API (mock)")
public class AiChatMockController {

    private final AiChatMockUseCase aiChatMockUseCase;

    @PostMapping(value = "/{chatId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "질문 보내기 (mock)", description = """
            고정 응답을 SSE 로 보냅니다. 이벤트 순서: progress → token(여러 번) → works → done, 실패하면 error.
            질문에 "결과없음" 을 넣으면 NO_RESULT, "없는작품" 은 WORK_NOT_FOUND, "작품외" 는 OFF_TOPIC, "오류" 는 error 이벤트로 끝납니다.
            앱은 모르는 status 를 받으면 text 대화로 보여 주세요.
            prod 에는 없고 실제 API 가 들어오면 교체됩니다.""")
    public SseEmitter sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody AiChatMessageRequest request
    ) {
        return aiChatMockUseCase.stream(chatId, request);
    }
}
