package com.storix.api.domain.aichat.controller;

import com.storix.api.domain.aichat.controller.dto.AiChatMessageRequest;
import com.storix.api.domain.aichat.usecase.AiChatMessageUseCase;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// 답변은 아직 mock 이 만들어서 prod 에는 없다. AI 서버를 붙이면 Profile 을 뗀다
@RestController
@RequestMapping("/api/v1/ai/chat")
@RequiredArgsConstructor
@Profile("!prod")
@Tag(name = "AI 작품 검색", description = "AI 작품 검색 API")
public class AiChatMessageController {

    private final AiChatMessageUseCase aiChatMessageUseCase;

    @PostMapping(value = "/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "[5] 질문 보내기", description = """
            질문을 저장하고 답변을 SSE 로 보냅니다. 이벤트 순서: progress → token(여러 번) → works → done, 실패하면 error.
            - progress: 진행 표시 ("작품 찾는 중…")
            - token: 말풍선에 글자를 이어 붙인다
            - works: 카드 0~5개. 배열 순서가 추천 순위
            - done: { messageId, status }. 입력창을 다시 켠다. messageId 는 이 답변의 ID
            - error: { code, message }. 오류 카드와 다시 시도. 다시 시도는 같은 질문을 다시 보낸다
            status 가 OK 이고 카드가 있으면 카드형, 그 밖(NO_RESULT · WORK_NOT_FOUND · OFF_TOPIC · 모르는 값)은 text 대화로 보여 주세요.
            19금 작품은 챗봇 설정에서 켰고 지금 성인 인증이 유효할 때만 나옵니다.

            지금 답변은 mock 이 만듭니다. 작품은 dev DB 에 있는 것으로 골라 카드 · 작품 상세가 실제로 이어집니다.
            질문에 "1개" 를 넣으면 카드 1개, "결과없음" 은 NO_RESULT, "없는작품" 은 WORK_NOT_FOUND, "작품외" 는 OFF_TOPIC, "오류" 는 error 로 끝납니다. 그 밖에는 카드 5개.
            prod 에는 아직 없습니다.""")
    public SseEmitter sendMessage(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @Valid @RequestBody AiChatMessageRequest request
    ) {
        return aiChatMessageUseCase.send(authUser.getUserId(), request);
    }
}
