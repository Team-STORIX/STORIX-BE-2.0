package com.storix.infrastructure.global.ratelimit;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("[요청 제한] 필터")
class RateLimitFilterTest {

    private final RateLimitCounter counter = mock(RateLimitCounter.class);
    private final RateLimitFilter filter = new RateLimitFilter(counter);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("한도 안이면 통과")
    void underLimit() throws Exception {
        when(counter.increment(anyString(), anyLong())).thenReturn(600L);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/works/1"), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("한도를 넘으면 429 예외와 Retry-After")
    void overLimit() {
        when(counter.increment(anyString(), anyLong())).thenReturn(601L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/works/1"), response, new MockFilterChain()))
                .isInstanceOf(STORIXCodeException.class)
                .extracting(e -> ((STORIXCodeException) e).getErrorCode())
                .isEqualTo(ErrorCode.TOO_MANY_REQUESTS);
        assertThat(response.getHeader("Retry-After")).isNotNull();
    }

    @Test
    @DisplayName("로그인 사용자는 userId 기준으로 센다")
    void countsByUser() throws Exception {
        AuthUserDetails user = new AuthUserDetails(7L, Role.READER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/works/1"), new MockHttpServletResponse(), new MockFilterChain());

        verify(counter).increment(startsWith("rate-limit:DEFAULT:user:7:"), anyLong());
    }

    @Test
    @DisplayName("비로그인은 X-Forwarded-For 마지막 IP 기준으로 센다")
    void countsByLastForwardedIp() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/tokens/refresh");
        request.addHeader("X-Forwarded-For", "1.1.1.1, 203.0.113.9");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(counter).increment(startsWith("rate-limit:AUTH:ip:203.0.113.9:"), anyLong());
    }

    @Test
    @DisplayName("내부 API · 헬스 체크 · OPTIONS 는 세지 않는다")
    void excluded() throws Exception {
        filter.doFilter(new MockHttpServletRequest("POST", "/internal/v1/works/import"), new MockHttpServletResponse(), new MockFilterChain());
        filter.doFilter(new MockHttpServletRequest("GET", "/actuator/health"), new MockHttpServletResponse(), new MockFilterChain());
        filter.doFilter(new MockHttpServletRequest("OPTIONS", "/api/v1/works/1"), new MockHttpServletResponse(), new MockFilterChain());

        verify(counter, never()).increment(anyString(), anyLong());
    }

    @Test
    @DisplayName("초과 로그는 구간마다 처음 넘었을 때 한 번만 남긴다")
    void logsOnlyFirstExceed() {
        Logger logger = (Logger) LoggerFactory.getLogger(RateLimitFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            when(counter.increment(anyString(), anyLong())).thenReturn(601L, 602L, 603L);
            for (int i = 0; i < 3; i++) {
                assertThatThrownBy(() -> filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/works/1"), new MockHttpServletResponse(), new MockFilterChain()))
                        .isInstanceOf(STORIXCodeException.class);
            }

            assertThat(appender.list).extracting(ILoggingEvent::getFormattedMessage)
                    .containsExactly(">>> [RateLimit] 요청 횟수 초과");
        } finally {
            logger.detachAppender(appender);
        }
    }
}
