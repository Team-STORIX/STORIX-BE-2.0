package com.storix.infrastructure.global.security;

import com.storix.common.utils.STORIXStatic;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[로깅] MDC 상관키 필터")
class MdcContextFilterTest {

    private final MdcContextFilter filter = new MdcContextFilter();

    private static final String HEADER = "X-Trace-Id";

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
        FilterChain chainPuttingUserId = (req, res) -> MDC.put(STORIXStatic.Mdc.USER_ID, "1");

        filter.doFilter(
                new MockHttpServletRequest("GET", "/api/v1/works/1"),
                new MockHttpServletResponse(),
                chainPuttingUserId
        );

        assertThat(MDC.get(STORIXStatic.Mdc.TRACE_ID)).isNull();
        assertThat(MDC.get(STORIXStatic.Mdc.ENDPOINT)).isNull();
        assertThat(MDC.get(STORIXStatic.Mdc.HTTP_METHOD)).isNull();
        assertThat(MDC.get(STORIXStatic.Mdc.USER_ID)).isNull();
    }
}
