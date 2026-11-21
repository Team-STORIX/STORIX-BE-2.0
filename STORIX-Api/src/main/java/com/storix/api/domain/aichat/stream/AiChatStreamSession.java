package com.storix.api.domain.aichat.stream;

import com.storix.api.domain.aichat.controller.dto.AiChatDoneEvent;
import com.storix.api.domain.aichat.controller.dto.AiChatErrorEvent;
import com.storix.api.domain.aichat.controller.dto.AiChatProgressEvent;
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

// 질문 하나의 답변 스트림. 앱으로 흘려보내면서 글자 · 추천 작품을 모아 두고, 끝나면 답변을 저장한다
// 앱 연결이 끊겨도 답변은 끝까지 받아 저장한다. 다음 진입 때 지난 대화에 보이도록
@Slf4j
public class AiChatStreamSession implements AiChatAnswerSink {

    private static final String SAVE_FAILED_MESSAGE = "일시적인 오류로 답변을 저장하지 못했어요. 잠시 후 다시 시도해 주세요.";

    private final AiChatQuestion question;
    private final SseEmitter emitter;
    private final AiChatMessageService aiChatMessageService;
    private final WorksCardService worksCardService;
    private final AiChatWorksJson aiChatWorksJson;

    private final StringBuilder content = new StringBuilder();
    private List<AiChatRawWork> works = List.of();
    private String pipelineVersion;
    private boolean connected = true;
    private boolean finished;

    public AiChatStreamSession(AiChatQuestion question, SseEmitter emitter, AiChatMessageService aiChatMessageService,
                               WorksCardService worksCardService, AiChatWorksJson aiChatWorksJson) {
        this.question = question;
        this.emitter = emitter;
        this.aiChatMessageService = aiChatMessageService;
        this.worksCardService = worksCardService;
        this.aiChatWorksJson = aiChatWorksJson;
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
        content.append(text);
        send("token", new AiChatTokenEvent(text));
    }

    // DB 에 없는 작품은 버린다. 순위는 AI 가 준 순서를 지킨다
    @Override
    public void works(List<AiChatRawWork> recommended, String pipelineVersion) {
        Map<Long, WorksCardInfo> cards = worksCardService.findCards(AiChatWorksJson.workIds(List.of(recommended)));
        this.works = recommended.stream()
                .filter(work -> cards.containsKey(work.workId()))
                .toList();
        this.pipelineVersion = pipelineVersion;
        List<AiChatRecommendedWork> cardsToSend = works.stream()
                .map(work -> AiChatRecommendedWork.of(work, cards.get(work.workId())))
                .toList();
        send("works", new AiChatWorksEvent(cardsToSend, pipelineVersion));
    }

    // 저장이 실패하면 앱에는 오류로 알린다. 스트림이 열린 채 남지 않게
    @Override
    public void done(AiChatAnswerStatus status) {
        try {
            Long answerId = aiChatMessageService.saveAnswer(question.chatId(), content.toString(), status,
                    aiChatWorksJson.write(works), pipelineVersion);
            send("done", new AiChatDoneEvent(answerId, status.name()));
        } catch (RuntimeException e) {
            log.error(">>> [AiChat] 답변 저장 실패 messageId={}", question.messageId(), e);
            send("error", new AiChatErrorEvent("INTERNAL", SAVE_FAILED_MESSAGE));
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
