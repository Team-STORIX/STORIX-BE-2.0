package com.storix.api.domain.aichat.stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;
import com.storix.domain.domains.aichat.domain.AiChatMessageRole;
import com.storix.domain.domains.aichat.domain.AiChatMessageType;
import com.storix.domain.domains.aichat.dto.AiChatMessageInfo;
import com.storix.domain.domains.works.domain.WorksType;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[AI 채팅] AI 서버 답변 받기")
class AiChatServerResponderTest {

    private static final String TOKEN = "test-ai-token";
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

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

    private AiChatServerResponder serve(int status, String body) throws IOException {
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

    private static AiChatServerResponder responder(String baseUrl) {
        return new AiChatServerResponder(RestClient.builder(), MAPPER, baseUrl, TOKEN);
    }

    private static AiChatQuestion question() {
        List<AiChatMessageInfo> recent = List.of(
                new AiChatMessageInfo(1L, AiChatMessageRole.USER, AiChatMessageType.TEXT, "로판", null, null, null),
                new AiChatMessageInfo(2L, AiChatMessageRole.CHATBOT, AiChatMessageType.TEXT, "이거요", AiChatAnswerStatus.OK, null, null));
        AiChatContext context = AiChatContext.of(recent, Map.of(2L, List.of(new AiChatRawWork(1, 7L, "이유", List.of()))));
        return new AiChatQuestion(1L, 10L, 100L, "다른 거", true, Set.of(WorksType.WEBTOON), List.of(3L), context);
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
        assertThat(receivedToken.get()).isEqualTo(TOKEN);

        JsonNode body = MAPPER.readTree(receivedBody.get());
        assertThat(body.get("chatId").asLong()).isEqualTo(10L);
        assertThat(body.get("messageId").asLong()).isEqualTo(100L);
        assertThat(body.get("question").asText()).isEqualTo("다른 거");
        assertThat(body.get("policy").get("adultAllowed").asBoolean()).isTrue();
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
    @DisplayName("done · error 없이 끊기면 아무것도 넘기지 않는다. 닫는 건 AiChatStreamer 몫")
    void endedWithoutDone() throws Exception {
        String sse = """
                event: token
                data: {"event":"token","text":"중간"}

                """;

        serve(200, sse).respond(question(), sink);

        assertThat(sink.events).containsExactly("token:중간");
    }
}
