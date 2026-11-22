package com.storix.api.domain.aichat.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.api.domain.aichat.stream.dto.AiServerDoneEvent;
import com.storix.api.domain.aichat.stream.dto.AiServerErrorEvent;
import com.storix.api.domain.aichat.stream.dto.AiServerProgressEvent;
import com.storix.api.domain.aichat.stream.dto.AiServerSearchRequest;
import com.storix.api.domain.aichat.stream.dto.AiServerTokenEvent;
import com.storix.api.domain.aichat.stream.dto.AiServerUsage;
import com.storix.api.domain.aichat.stream.dto.AiServerWorksEvent;
import com.storix.common.utils.STORIXStatic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@Profile("!prod")
@ConditionalOnProperty(name = "ai.chat.responder", havingValue = "server")
public class AiChatServerResponder implements AiChatResponder {

    private static final String STREAM_PATH = "/internal/search/stream";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(35);
    private static final int MAX_WORKS = 5;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AiChatServerResponder(RestClient.Builder restClientBuilder,
                                 ObjectMapper objectMapper,
                                 @Value("${ai.server.base-url}") String baseUrl,
                                 @Value("${internal.ai-token}") String aiToken) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        // AI 서버는 조용할 때 15초마다 heartbeat 를 보낸다. 두 번 넘게 아무것도 안 오면 끊긴 것으로 본다
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(STORIXStatic.Internal.AI_TOKEN_HEADER, aiToken)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public void respond(AiChatQuestion question, AiChatAnswerSink sink) {
        try {
            restClient.post()
                    .uri(STREAM_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .body(AiServerSearchRequest.from(question))
                    .exchange((request, response) -> {
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            log.atError()
                                    .addKeyValue("messageId", question.messageId())
                                    .addKeyValue("status", response.getStatusCode().value())
                                    .log(">>> [AiChat] AI 서버 요청 거절");
                            sink.error(AiServerErrorEvent.INTERNAL, AiServerErrorEvent.appMessage(AiServerErrorEvent.INTERNAL));
                            return null;
                        }
                        // SSE 프레임은 event · data 줄 뒤 빈 줄. ':' 로 시작하는 heartbeat 줄은 건너뛴다
                        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
                        String event = null;
                        StringBuilder data = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                if (event != null && dispatch(question, event, data.toString(), sink)) {
                                    return null;
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
                        return null;
                    });
        } catch (ResourceAccessException e) {
            String code = e.getCause() instanceof SocketTimeoutException
                    ? AiServerErrorEvent.TIMEOUT
                    : AiServerErrorEvent.INTERNAL;
            log.atWarn()
                    .addKeyValue("messageId", question.messageId())
                    .addKeyValue("code", code)
                    .setCause(e)
                    .log(">>> [AiChat] AI 서버 응답 받기 실패");
            sink.error(code, AiServerErrorEvent.appMessage(code));
        }
        // done · error 없이 끝나면 AiChatStreamer 가 오류로 닫는다
    }

    // 끝을 알리는 이벤트(done · error)면 true
    private boolean dispatch(AiChatQuestion question, String event, String data, AiChatAnswerSink sink) throws IOException {
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
                List<AiChatRawWork> trusted = works.trustedWorks(question.excludedWorkIds(), MAX_WORKS);
                int received = works.recommendedWorks() == null ? 0 : works.recommendedWorks().size();
                if (trusted.size() < Math.min(received, MAX_WORKS)) {
                    log.atWarn()
                            .addKeyValue("messageId", question.messageId())
                            .addKeyValue("received", received)
                            .addKeyValue("trusted", trusted.size())
                            .log(">>> [AiChat] 후보 밖 · 중복 · 서재 추천 작품 버림");
                }
                sink.works(trusted, works.pipelineVersion());
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
