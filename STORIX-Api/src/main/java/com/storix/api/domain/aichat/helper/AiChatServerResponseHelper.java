package com.storix.api.domain.aichat.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.api.domain.aichat.controller.dto.AiChatMatchedClue;
import com.storix.api.domain.aichat.controller.dto.AiChatQuestion;
import com.storix.api.domain.aichat.controller.dto.AiChatRawWork;
import com.storix.domain.domains.aichat.dto.AiChatMessageInfo;
import com.storix.domain.domains.aichat.service.AiChatMessageService;
import com.storix.domain.domains.library.service.LibraryService;
import com.storix.infrastructure.external.aichat.client.AiServerClient;
import com.storix.infrastructure.external.aichat.dto.AiServerChatContext;
import com.storix.infrastructure.external.aichat.dto.AiServerDoneEvent;
import com.storix.infrastructure.external.aichat.dto.AiServerErrorEvent;
import com.storix.infrastructure.external.aichat.dto.AiServerProgressEvent;
import com.storix.infrastructure.external.aichat.dto.AiServerRecommendedWork;
import com.storix.infrastructure.external.aichat.dto.AiServerSearchPolicy;
import com.storix.infrastructure.external.aichat.dto.AiServerSearchRequest;
import com.storix.infrastructure.external.aichat.dto.AiServerTokenEvent;
import com.storix.infrastructure.external.aichat.dto.AiServerUsage;
import com.storix.infrastructure.external.aichat.dto.AiServerWorksEvent;
import feign.Response;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@Profile("!prod")
@ConditionalOnProperty(name = "ai.chat.responder", havingValue = "server")
@RequiredArgsConstructor
public class AiChatServerResponseHelper implements AiChatResponder {

    private static final int MAX_WORKS = 5;

    private final AiServerClient aiServerClient;
    private final ObjectMapper objectMapper;
    private final AiChatMessageService aiChatMessageService;
    private final LibraryService libraryService;
    private final AiChatWorksJsonHelper aiChatWorksJsonHelper;

    // done · error 없이 끝나면 AiChatStreamHelper 가 오류로 닫는다
    @Override
    public void respond(AiChatQuestion question, AiChatAnswerSink sink) {
        List<AiChatMessageInfo> recent = aiChatMessageService.findContext(question.chatId(), question.messageId(),
                AiServerChatContext.MAX_TURNS);
        List<Long> libraryWorkIds = libraryService.findReviewedWorksIds(question.userId());
        AiServerSearchRequest request = new AiServerSearchRequest(
                question.chatId(),
                question.messageId(),
                question.userId(),
                question.question(),
                new AiServerSearchPolicy(
                        question.adultAllowed(),
                        question.worksTypes(),
                        libraryWorkIds
                ),
                AiServerChatContext.of(recent, aiChatWorksJsonHelper.readWorkIds(recent))
        );

        try (Response response = aiServerClient.searchStream(request)) {
            if (response.status() / 100 != 2 || response.body() == null) {
                log.atError()
                        .addKeyValue("messageId", question.messageId())
                        .addKeyValue("status", response.status())
                        .log(">>> [AiChat] AI 서버 요청 거절");
                sink.error(AiServerErrorEvent.INTERNAL, AiServerErrorEvent.appMessage(AiServerErrorEvent.INTERNAL));
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8));
            String event = null;
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    if (event != null && dispatch(question, libraryWorkIds, event, data.toString(), sink)) {
                        return;
                    }
                    event = null;
                    data.setLength(0);
                } else if (line.startsWith("event:")) {
                    event = line.substring("event:".length()).trim();
                } else if (line.startsWith("data:")) {
                    if (!data.isEmpty()) {
                        data.append('\n');
                    }
                    data.append(line.substring("data:".length()).stripLeading());
                }
            }
        } catch (RetryableException | IOException e) {
            Throwable cause = e instanceof RetryableException ? e.getCause() : e;
            String code = cause instanceof SocketTimeoutException
                    ? AiServerErrorEvent.TIMEOUT
                    : AiServerErrorEvent.INTERNAL;
            log.atWarn()
                    .addKeyValue("messageId", question.messageId())
                    .addKeyValue("code", code)
                    .setCause(e)
                    .log(">>> [AiChat] AI 서버 응답 받기 실패");
            sink.error(code, AiServerErrorEvent.appMessage(code));
        }
    }

    private boolean dispatch(AiChatQuestion question, List<Long> libraryWorkIds, String event, String data,
                             AiChatAnswerSink sink) throws IOException {
        switch (event) {
            case "progress" -> {
                AiServerProgressEvent progress = objectMapper.readValue(data, AiServerProgressEvent.class);
                sink.progress(progress.step(), progress.message());
                return false;
            }
            case "token" -> {
                sink.token(objectMapper.readValue(data, AiServerTokenEvent.class).text());
                return false;
            }
            case "works" -> {
                AiServerWorksEvent works = objectMapper.readValue(data, AiServerWorksEvent.class);
                List<AiServerRecommendedWork> trusted = works.trustedWorks(libraryWorkIds, MAX_WORKS);
                int received = works.recommendedWorks() == null ? 0 : works.recommendedWorks().size();
                if (trusted.size() < Math.min(received, MAX_WORKS)) {
                    log.atWarn()
                            .addKeyValue("messageId", question.messageId())
                            .addKeyValue("received", received)
                            .addKeyValue("trusted", trusted.size())
                            .log(">>> [AiChat] 후보 밖 · 중복 · 서재 추천 작품 버림");
                }
                sink.works(trusted.stream()
                        .map(work -> new AiChatRawWork(
                                work.rank(),
                                work.workId(),
                                work.reason(),
                                work.matchedClues().stream()
                                        .map(clue -> new AiChatMatchedClue(clue.text(), clue.source()))
                                        .toList()
                        ))
                        .toList(), works.pipelineVersion());
                return false;
            }
            case "done" -> {
                AiServerDoneEvent done = objectMapper.readValue(data, AiServerDoneEvent.class);
                for (AiServerUsage usage : done.usage() == null ? List.<AiServerUsage>of() : done.usage()) {
                    log.atInfo()
                            .addKeyValue("messageId", question.messageId())
                            .addKeyValue("model", usage.model())
                            .addKeyValue("inputTokens", usage.inputTokens())
                            .addKeyValue("outputTokens", usage.outputTokens())
                            .addKeyValue("cacheReadTokens", usage.cacheReadTokens())
                            .addKeyValue("elapsedMs", done.elapsedMs())
                            .log(">>> [AiChat] AI 답변 사용량");
                }
                done.answerStatus().ifPresentOrElse(sink::done, () -> {
                    log.atError()
                            .addKeyValue("messageId", question.messageId())
                            .addKeyValue("status", done.status())
                            .log(">>> [AiChat] AI 서버가 모르는 결과 상태를 보냄");
                    sink.error(AiServerErrorEvent.INTERNAL, AiServerErrorEvent.appMessage(AiServerErrorEvent.INTERNAL));
                });
                return true;
            }
            case "error" -> {
                AiServerErrorEvent error = objectMapper.readValue(data, AiServerErrorEvent.class);
                log.atWarn()
                        .addKeyValue("messageId", question.messageId())
                        .addKeyValue("code", error.code())
                        .log(">>> [AiChat] AI 서버 오류 응답");
                sink.error(error.appCode(), AiServerErrorEvent.appMessage(error.appCode()));
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
