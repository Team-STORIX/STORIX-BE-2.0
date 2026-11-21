package com.storix.api.domain.aichat.stream;

import com.storix.domain.domains.aichat.service.AiChatMessageService;
import com.storix.domain.domains.works.service.WorksCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

// 답변 스트림을 별도 스레드에서 돌린다. 요청 스레드 · DB 커넥션을 스트리밍 동안 잡지 않는다
@Slf4j
@Component
@Profile("!prod")
public class AiChatStreamer {

    private static final long EMITTER_TIMEOUT_MILLIS = 120_000L;
    private static final String INTERNAL_ERROR_MESSAGE = "일시적인 오류로 검색하지 못했어요. 잠시 후 다시 시도해 주세요.";

    private final Executor aiChatStreamExecutor;
    private final AiChatResponder aiChatResponder;
    private final AiChatMessageService aiChatMessageService;
    private final WorksCardService worksCardService;
    private final AiChatWorksJson aiChatWorksJson;

    public AiChatStreamer(@Qualifier("aiChatStreamExecutor") Executor aiChatStreamExecutor,
                          AiChatResponder aiChatResponder,
                          AiChatMessageService aiChatMessageService,
                          WorksCardService worksCardService,
                          AiChatWorksJson aiChatWorksJson) {
        this.aiChatStreamExecutor = aiChatStreamExecutor;
        this.aiChatResponder = aiChatResponder;
        this.aiChatMessageService = aiChatMessageService;
        this.worksCardService = worksCardService;
        this.aiChatWorksJson = aiChatWorksJson;
    }

    public SseEmitter start(AiChatQuestion question) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MILLIS);
        AiChatStreamSession session = new AiChatStreamSession(question, emitter, aiChatMessageService,
                worksCardService, aiChatWorksJson);
        try {
            aiChatStreamExecutor.execute(() -> run(question, session));
        } catch (RejectedExecutionException e) {
            log.warn(">>> [AiChat] 답변 스트림 실행기 포화 messageId={}", question.messageId());
            session.error("INTERNAL", INTERNAL_ERROR_MESSAGE);
        }
        return emitter;
    }

    private void run(AiChatQuestion question, AiChatStreamSession session) {
        try {
            aiChatResponder.respond(question, session);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            closeWithError(question, session, e);
        } catch (RuntimeException e) {
            closeWithError(question, session, e);
        }
        if (!session.isFinished()) {
            log.error(">>> [AiChat] 답변이 done · error 없이 끝남 messageId={}", question.messageId());
            session.error("INTERNAL", INTERNAL_ERROR_MESSAGE);
        }
    }

    private void closeWithError(AiChatQuestion question, AiChatStreamSession session, Exception e) {
        log.error(">>> [AiChat] 답변 생성 실패 messageId={}", question.messageId(), e);
        if (!session.isFinished()) {
            session.error("INTERNAL", INTERNAL_ERROR_MESSAGE);
        }
    }
}
