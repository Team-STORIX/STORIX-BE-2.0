package com.storix.api.domain.aichat.helper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.storix.api.domain.aichat.controller.dto.AiChatQuestion;
import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;
import com.storix.domain.domains.aichat.domain.AiChatMessageRole;
import com.storix.domain.domains.aichat.domain.AiChatMessageType;
import com.storix.domain.domains.aichat.dto.AiChatMessageInfo;
import com.storix.domain.domains.aichat.service.AiChatMessageService;
import com.storix.domain.domains.library.service.LibraryService;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.infrastructure.external.aichat.client.AiServerClient;
import com.storix.infrastructure.external.aichat.config.AiServerClientConfig;
import com.storix.infrastructure.external.aichat.dto.AiServerChatContext;
import com.sun.net.httpserver.HttpServer;
import feign.Feign;
import feign.Retryer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("[AI 채팅] AI 서버 답변 받기")
class AiChatServerResponseHelperTest {

    private static final String TOKEN = "test-ai-token";
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Mock
    private AiChatMessageService aiChatMessageService;
    @Mock
    private LibraryService libraryService;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private HttpServer server;
    private final AtomicReference<String> receivedToken = new AtomicReference<>();
    private final AtomicReference<String> receivedBody = new AtomicReference<>();
    private final RecordingAnswerSink sink = new RecordingAnswerSink();

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    private AiChatServerResponseHelper serve(int status, String body) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/internal/search/stream", exchange -> {
            receivedToken.set(exchange.getRequestHeaders().getFirst("X-Internal-Token"));
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();
        return responder("http://127.0.0.1:" + server.getAddress().getPort());
    }

    private AiChatServerResponseHelper responder(String baseUrl) {
        AiServerClientConfig config = new AiServerClientConfig();
        AiServerClient client = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(config.aiServerEncoder(MAPPER))
                .options(config.aiServerRequestOptions())
                .requestInterceptor(config.aiServerTokenInterceptor(TOKEN))
                .retryer(Retryer.NEVER_RETRY)
                .target(AiServerClient.class, baseUrl);
        given(aiChatMessageService.findContext(10L, 100L, AiServerChatContext.MAX_TURNS)).willReturn(List.of(
                new AiChatMessageInfo(1L, AiChatMessageRole.USER, AiChatMessageType.TEXT, "로판", null, null, null),
                new AiChatMessageInfo(2L, AiChatMessageRole.CHATBOT, AiChatMessageType.TEXT, "이거요", AiChatAnswerStatus.OK,
                        "[{\"rank\":1,\"workId\":7,\"reason\":\"이유\",\"matchedClues\":[]}]", null)));
        given(libraryService.findReviewedWorksIds(1L)).willReturn(List.of(3L));
        return new AiChatServerResponseHelper(client, MAPPER, aiChatMessageService, libraryService,
                new AiChatWorksJsonHelper(MAPPER), new AiChatMetricsHelper(meterRegistry, "server"));
    }

    private static AiChatQuestion question() {
        return new AiChatQuestion(1L, 10L, 100L, "다른 거", true, false, Set.of(WorksType.WEBTOON));
    }

    @Test
    @DisplayName("heartbeat 는 건너뛰고 progress · token · works · done 을 순서대로 넘긴다")
    void relayStream() throws Exception {
        String sse = """
                : ping

                event: progress
                data: {"event":"progress","step":"SEARCHING","message":"작품 찾는 중…"}

                event: token
                data: {"event":"token","text":"이건 "}

                : ping

                event: token
                data: {"event":"token","text":"어때요"}

                event: works
                data: {"event":"works","recommendedWorks":[{"rank":1,"workId":3,"reason":"서재","matchedClues":[]},{"rank":2,"workId":5,"reason":"좋아요","matchedClues":[{"text":"로판","source":"GENRE"}]},{"rank":3,"workId":99,"reason":"후보 밖","matchedClues":[]}],"candidateWorkIds":[3,5,6],"pipelineVersion":"v1"}

                event: done
                data: {"event":"done","status":"OK","usage":[{"model":"claude-sonnet-5-5","inputTokens":10,"outputTokens":5,"cacheReadTokens":0}],"elapsedMs":1200}

                event: token
                data: {"event":"token","text":"done 뒤는 무시"}

                """;

        serve(200, sse).respond(question(), sink);

        assertThat(sink.events).containsExactly(
                "progress:SEARCHING:작품 찾는 중…",
                "token:이건 ",
                "token:어때요",
                "works:v1:[1=5]",
                "done:OK");
        assertThat(meterRegistry.get("ai.chat.tokens").tags("model", "claude-sonnet-5-5", "type", "input").counter().count()).isEqualTo(10);
        assertThat(meterRegistry.get("ai.chat.tokens").tags("model", "claude-sonnet-5-5", "type", "output").counter().count()).isEqualTo(5);
        assertThat(receivedToken.get()).isEqualTo(TOKEN);

        JsonNode body = MAPPER.readTree(receivedBody.get());
        assertThat(body.get("chatId").asLong()).isEqualTo(10L);
        assertThat(body.get("messageId").asLong()).isEqualTo(100L);
        assertThat(body.get("question").asText()).isEqualTo("다른 거");
        assertThat(body.get("policy").get("adultDefault").asBoolean()).isTrue();
        assertThat(body.get("policy").get("adultVerified").asBoolean()).isFalse();
        assertThat(body.get("policy").get("workTypes").get(0).asText()).isEqualTo("WEBTOON");
        assertThat(body.get("policy").get("excludedWorkIds").get(0).asLong()).isEqualTo(3L);
        assertThat(body.get("context").get("turns").get(1).get("role").asText()).isEqualTo("assistant");
        assertThat(body.get("context").get("turns").get(1).get("workIds").get(0).asLong()).isEqualTo(7L);
        assertThat(body.get("context").get("shownWorkIds").get(0).asLong()).isEqualTo(7L);
    }

    @Test
    @DisplayName("AI 서버 error 는 앱 코드 · Spring 문구로 바꿔 넘긴다")
    void relayError() throws Exception {
        String sse = """
                event: error
                data: {"event":"error","code":"INVALID_REQUEST","message":"bad"}

                """;

        serve(200, sse).respond(question(), sink);

        assertThat(sink.events).containsExactly("error:INTERNAL:일시적인 오류로 검색하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }

    @Test
    @DisplayName("모르는 done 상태는 오류로 닫는다")
    void unknownStatus() throws Exception {
        String sse = """
                event: done
                data: {"event":"done","status":"ERROR","usage":[],"elapsedMs":1}

                """;

        serve(200, sse).respond(question(), sink);

        assertThat(sink.events).containsExactly("error:INTERNAL:일시적인 오류로 검색하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }

    @Test
    @DisplayName("401 같은 거절 응답은 INTERNAL 오류로 넘긴다")
    void rejected() throws Exception {
        serve(401, "{\"detail\":\"unauthorized\"}").respond(question(), sink);

        assertThat(sink.events).containsExactly("error:INTERNAL:일시적인 오류로 검색하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }

    @Test
    @DisplayName("AI 서버에 연결하지 못하면 INTERNAL 오류로 넘긴다")
    void unreachable() throws Exception {
        responder("http://127.0.0.1:1").respond(question(), sink);

        assertThat(sink.events).containsExactly("error:INTERNAL:일시적인 오류로 검색하지 못했어요. 잠시 후 다시 시도해 주세요.");
    }

    @Test
    @DisplayName("done · error 없이 끊기면 아무것도 넘기지 않는다. 닫는 건 AiChatStreamHelper 몫")
    void endedWithoutDone() throws Exception {
        String sse = """
                event: token
                data: {"event":"token","text":"중간"}

                """;

        serve(200, sse).respond(question(), sink);

        assertThat(sink.events).containsExactly("token:중간");
    }
}
