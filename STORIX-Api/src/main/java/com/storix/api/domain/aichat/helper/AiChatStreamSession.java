package com.storix.api.domain.aichat.helper;

import com.storix.api.domain.aichat.controller.dto.AiChatDoneEvent;
import com.storix.api.domain.aichat.controller.dto.AiChatErrorEvent;
import com.storix.api.domain.aichat.controller.dto.AiChatProgressEvent;
import com.storix.api.domain.aichat.controller.dto.AiChatQuestion;
import com.storix.api.domain.aichat.controller.dto.AiChatRawWork;
import com.storix.api.domain.aichat.controller.dto.AiChatRecommendedWork;
import com.storix.api.domain.aichat.controller.dto.AiChatTokenEvent;
import com.storix.api.domain.aichat.controller.dto.AiChatWorksEvent;
import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;
import com.storix.domain.domains.aichat.service.AiChatMessageService;
import com.storix.domain.domains.works.dto.WorksCardInfo;
import com.storix.domain.domains.works.service.WorksCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class AiChatStreamSession implements AiChatAnswerSink {

    private static final String SAVE_FAILED_MESSAGE = "일시적인 오류로 답변을 저장하지 못했어요. 잠시 후 다시 시도해 주세요.";

    private final AiChatQuestion question;
    private final SseEmitter emitter;
    private final AiChatMessageService aiChatMessageService;
    private final WorksCardService worksCardService;
    private final AiChatWorksJsonHelper aiChatWorksJson;
    private final AiChatMetricsHelper aiChatMetricsHelper;
    private final AiChatUsageLimitHelper aiChatUsageLimitHelper;
    private final long startedNanos = System.nanoTime();

    private final StringBuilder content = new StringBuilder();
    private List<AiChatRawWork> works = List.of();
    private String pipelineVersion;
    private boolean connected = true;
    private boolean finished;
    private boolean firstTokenSeen;

    public AiChatStreamSession(AiChatQuestion question, SseEmitter emitter, AiChatMessageService aiChatMessageService,
                               WorksCardService worksCardService, AiChatWorksJsonHelper aiChatWorksJson,
                               AiChatMetricsHelper aiChatMetricsHelper,
                               AiChatUsageLimitHelper aiChatUsageLimitHelper) {
        this.question = question;
        this.emitter = emitter;
        this.aiChatMessageService = aiChatMessageService;
        this.worksCardService = worksCardService;
        this.aiChatWorksJson = aiChatWorksJson;
        this.aiChatMetricsHelper = aiChatMetricsHelper;
        this.aiChatUsageLimitHelper = aiChatUsageLimitHelper;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public void progress(String step, String message) {
        send("progress", new AiChatProgressEvent(step, message));
    }

    @Override
    public void token(String text) {
        if (!firstTokenSeen) {
            firstTokenSeen = true;
            aiChatMetricsHelper.recordFirstToken(System.nanoTime() - startedNanos);
        }
        content.append(text);
        send("token", new AiChatTokenEvent(text));
    }

    @Override
    public void works(List<AiChatRawWork> recommended, String pipelineVersion) {
        Map<Long, WorksCardInfo> cards = worksCardService.findCards(AiChatWorksJsonHelper.workIds(List.of(recommended)));
        this.works = recommended.stream()
                .filter(work -> cards.containsKey(work.workId()))
                .filter(work -> question.adultAllowed() || !cards.get(work.workId()).adult())
                .toList();
        this.pipelineVersion = pipelineVersion;
        List<AiChatRecommendedWork> cardsToSend = works.stream()
                .map(work -> AiChatRecommendedWork.of(work, cards.get(work.workId())))
                .toList();
        send("works", new AiChatWorksEvent(cardsToSend, pipelineVersion));
    }

    @Override
    public void done(AiChatAnswerStatus answerStatus) {
        AiChatAnswerStatus status = answerStatus == AiChatAnswerStatus.OK && works.isEmpty()
                ? AiChatAnswerStatus.NO_RESULT
                : answerStatus;
        try {
            Long answerId = aiChatMessageService.saveAnswer(question.chatId(), content.toString(), status,
                    aiChatWorksJson.write(works), pipelineVersion);
            send("done", new AiChatDoneEvent(answerId, status.name()));
            aiChatMetricsHelper.recordAnswer(status.name(), AiChatMetricsHelper.NO_ERROR, System.nanoTime() - startedNanos);
            aiChatUsageLimitHelper.record(question.userId());
        } catch (RuntimeException e) {
            log.error(">>> [AiChat] 답변 저장 실패 messageId={}", question.messageId(), e);
            send("error", new AiChatErrorEvent("INTERNAL", SAVE_FAILED_MESSAGE));
            aiChatMetricsHelper.recordAnswer(AiChatAnswerStatus.ERROR.name(), "INTERNAL", System.nanoTime() - startedNanos);
        }
        finish();
    }

    @Override
    public void error(String code, String message) {
        try {
            aiChatMessageService.saveAnswer(question.chatId(), message, AiChatAnswerStatus.ERROR, null, pipelineVersion);
        } catch (RuntimeException e) {
            log.error(">>> [AiChat] 오류 답변 저장 실패 messageId={}", question.messageId(), e);
        }
        send("error", new AiChatErrorEvent(code, message));
        aiChatMetricsHelper.recordAnswer(AiChatAnswerStatus.ERROR.name(), code, System.nanoTime() - startedNanos);
        finish();
    }

    private void send(String name, Object data) {
        if (!connected) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().name(name).data(data, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            connected = false;
            log.info(">>> [AiChat] 앱 연결 끊김, 답변은 끝까지 받아 저장 messageId={}", question.messageId());
        }
    }

    private void finish() {
        finished = true;
        if (connected) {
            emitter.complete();
        }
    }
}
