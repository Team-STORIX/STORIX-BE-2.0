package com.storix.infrastructure.global.security;

import com.storix.common.utils.STORIXStatic;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MdcContextFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,64}");

    // 주기적으로 호출돼 추적 가치가 없는 경로
    private static final List<String> UNLOGGED_URI_PREFIXES = List.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
        response.setHeader(TRACE_ID_HEADER, traceId);

        String uri = request.getRequestURI();

        MDC.put(STORIXStatic.Mdc.TRACE_ID, traceId);
        MDC.put(STORIXStatic.Mdc.ENDPOINT, uri);
        MDC.put(STORIXStatic.Mdc.HTTP_METHOD, request.getMethod());

        boolean logged = isLogged(uri);
        long startedAt = System.nanoTime();

        if (logged) {
            String query = request.getQueryString();
            if (query == null) {
                log.info(">>> [Http] 요청 진입");
            } else {
                log.info(">>> [Http] 요청 진입 query={}", query);
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (logged) {
                log.info(">>> [Http] 요청 완료 status={} tookMs={}",
                        response.getStatus(),
                        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt));
            }
            MDC.remove(STORIXStatic.Mdc.TRACE_ID);
            MDC.remove(STORIXStatic.Mdc.ENDPOINT);
            MDC.remove(STORIXStatic.Mdc.HTTP_METHOD);
            MDC.remove(STORIXStatic.Mdc.USER_ID);
            MDC.remove(STORIXStatic.Mdc.ROLE);
        }
    }

    private boolean isLogged(String uri) {
        return UNLOGGED_URI_PREFIXES.stream().noneMatch(uri::startsWith);
    }

    private String resolveTraceId(String fromHeader) {
        if (fromHeader != null && TRACE_ID_PATTERN.matcher(fromHeader).matches()) {
            return fromHeader;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}
