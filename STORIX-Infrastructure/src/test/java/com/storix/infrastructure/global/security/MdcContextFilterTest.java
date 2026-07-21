package com.storix.infrastructure.global.security;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.storix.common.utils.STORIXStatic;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[로깅] MDC 상관키 필터")
class MdcContextFilterTest {

    private final MdcContextFilter filter = new MdcContextFilter();

    private static final String HEADER = "X-Trace-Id";

    private final Logger filterLogger = (Logger) LoggerFactory.getLogger(MdcContextFilter.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @BeforeEach
    void attachAppender() {
        appender.start();
        filterLogger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        filterLogger.detachAppender(appender);
        appender.stop();
    }

    private List<String> loggedMessages() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    // 필터가 finally 에서 지우므로 체인이 도는 순간에 떠둔다
    private Map<String, String> captured;

    private final FilterChain capturingChain = (req, res) -> {
        Map<String, String> snapshot = MDC.getCopyOfContextMap();
        captured = snapshot == null ? new HashMap<>() : new HashMap<>(snapshot);
    };

    private MockHttpServletResponse run(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, capturingChain);
        return response;
    }

    @Test
    @DisplayName("헤더가 없으면 서버가 traceId 를 만들고 응답 헤더로 돌려준다")
    void generatesTraceIdWhenHeaderAbsent() throws Exception {
        MockHttpServletResponse response = run(new MockHttpServletRequest("GET", "/api/v1/works/1"));

        String traceId = captured.get(STORIXStatic.Mdc.TRACE_ID);
        assertThat(traceId).hasSize(32);
        assertThat(response.getHeader(HEADER)).isEqualTo(traceId);
    }

    @Test
    @DisplayName("endpoint / httpMethod 가 MDC 에 담긴다")
    void putsRequestContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/reviews");
        run(request);

        assertThat(captured.get(STORIXStatic.Mdc.ENDPOINT)).isEqualTo("/api/v1/reviews");
        assertThat(captured.get(STORIXStatic.Mdc.HTTP_METHOD)).isEqualTo("POST");
    }

    @Test
    @DisplayName("형식에 맞는 헤더는 그대로 쓴다")
    void keepsValidHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/works/1");
        request.addHeader(HEADER, "a1b2c3d4");

        MockHttpServletResponse response = run(request);

        assertThat(captured.get(STORIXStatic.Mdc.TRACE_ID)).isEqualTo("a1b2c3d4");
        assertThat(response.getHeader(HEADER)).isEqualTo("a1b2c3d4");
    }

    @Test
    @DisplayName("너무 길거나(64자 초과) 이상한 문자가 섞인 헤더는 버리고 새로 만든다")
    void rejectsMalformedHeader() throws Exception {
        String tooLong = "a".repeat(65);
        String withNewline = "abc\ndef";
        String blank = "";

        for (String bad : new String[]{tooLong, withNewline, blank}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/works/1");
            request.addHeader(HEADER, bad);

            run(request);

            assertThat(captured.get(STORIXStatic.Mdc.TRACE_ID))
                    .as("버려야 할 헤더: %s", bad)
                    .isNotEqualTo(bad)
                    .hasSize(32);
        }
    }

    @Test
    @DisplayName("요청이 끝나면 MDC 를 비운다 — 스레드 재사용 시 남으면 안 된다")
    void clearsMdcAfterRequest() throws Exception {
        // userId·role 은 뒤에 오는 JwtAuthenticationFilter 가 채운다
        FilterChain chainPuttingPrincipal = (req, res) -> {
            MDC.put(STORIXStatic.Mdc.USER_ID, "1");
            MDC.put(STORIXStatic.Mdc.ROLE, "READER");
        };

        filter.doFilter(
                new MockHttpServletRequest("GET", "/api/v1/works/1"),
                new MockHttpServletResponse(),
                chainPuttingPrincipal
        );

        assertThat(MDC.get(STORIXStatic.Mdc.TRACE_ID)).isNull();
        assertThat(MDC.get(STORIXStatic.Mdc.ENDPOINT)).isNull();
        assertThat(MDC.get(STORIXStatic.Mdc.HTTP_METHOD)).isNull();
        assertThat(MDC.get(STORIXStatic.Mdc.USER_ID)).isNull();
        assertThat(MDC.get(STORIXStatic.Mdc.ROLE)).isNull();
    }

    @Test
    @DisplayName("관리자 경로가 아니어도 진입·완료 로그를 남긴다")
    void logsEveryRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/works/1");
        request.setQueryString("page=0");

        run(request);

        List<String> messages = loggedMessages();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isEqualTo(">>> [Http] 요청 진입 query=page=0");
        assertThat(messages.get(1)).matches(">>> \\[Http] 요청 완료 status=200 tookMs=\\d+");
    }

    @Test
    @DisplayName("쿼리스트링이 없으면 진입 로그에 붙이지 않는다")
    void omitsQueryWhenAbsent() throws Exception {
        run(new MockHttpServletRequest("GET", "/api/v1/works/1"));

        assertThat(loggedMessages().get(0)).isEqualTo(">>> [Http] 요청 진입");
    }

    @Test
    @DisplayName("actuator 등 추적 가치가 없는 경로는 로그를 남기지 않는다")
    void skipsUnloggedPaths() throws Exception {
        run(new MockHttpServletRequest("GET", "/actuator/prometheus"));

        assertThat(loggedMessages()).isEmpty();
        assertThat(captured.get(STORIXStatic.Mdc.TRACE_ID)).hasSize(32);
    }
}
