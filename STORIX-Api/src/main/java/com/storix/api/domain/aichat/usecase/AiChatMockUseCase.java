package com.storix.api.domain.aichat.usecase;

import com.storix.api.domain.aichat.controller.dto.AiChatMessageRequest;
import com.storix.api.domain.aichat.mock.AiChatMockEvent;
import com.storix.api.domain.aichat.mock.AiChatMockScript;
import com.storix.common.annotation.UseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executor;

@Slf4j
@UseCase
public class AiChatMockUseCase {

    private static final long EMITTER_TIMEOUT_MILLIS = 30_000L;

    private final Executor aiChatMockExecutor;

    public AiChatMockUseCase(@Qualifier("aiChatMockExecutor") Executor aiChatMockExecutor) {
        this.aiChatMockExecutor = aiChatMockExecutor;
    }

    public SseEmitter stream(Long chatId, AiChatMessageRequest request) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MILLIS);
        aiChatMockExecutor.execute(() -> {
            try {
                for (AiChatMockEvent event : AiChatMockScript.of(chatId, request.question())) {
                    Thread.sleep(event.delayMillis());
                    emitter.send(SseEmitter.event().name(event.name()).data(event.data(), MediaType.APPLICATION_JSON));
                }
                emitter.complete();
            } catch (IOException e) {
                log.info(">>>> [AI 채팅 mock] 클라이언트 연결 끊김 chatId={}", chatId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}
