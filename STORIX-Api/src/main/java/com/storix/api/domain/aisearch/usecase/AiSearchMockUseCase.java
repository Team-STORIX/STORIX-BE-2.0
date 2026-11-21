package com.storix.api.domain.aisearch.usecase;

import com.storix.api.domain.aisearch.controller.dto.AiSearchMessageRequest;
import com.storix.api.domain.aisearch.mock.AiSearchMockEvent;
import com.storix.api.domain.aisearch.mock.AiSearchMockScript;
import com.storix.common.annotation.UseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executor;

@Slf4j
@UseCase
public class AiSearchMockUseCase {

    private static final long EMITTER_TIMEOUT_MILLIS = 30_000L;

    private final Executor aiSearchMockExecutor;

    public AiSearchMockUseCase(@Qualifier("aiSearchMockExecutor") Executor aiSearchMockExecutor) {
        this.aiSearchMockExecutor = aiSearchMockExecutor;
    }

    public SseEmitter stream(Long chatId, AiSearchMessageRequest request) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MILLIS);
        aiSearchMockExecutor.execute(() -> {
            try {
                for (AiSearchMockEvent event : AiSearchMockScript.of(chatId, request.question())) {
                    Thread.sleep(event.delayMillis());
                    emitter.send(SseEmitter.event().name(event.name()).data(event.data(), MediaType.APPLICATION_JSON));
                }
                emitter.complete();
            } catch (IOException e) {
                log.info(">>>> [AI 검색 mock] 클라이언트 연결 끊김 chatId={}", chatId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}
