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
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MdcContextFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,64}");
    private static final String ADMIN_URI_PREFIX = "/api/v1/admin";
    private static final String ADMIN_AUTH_URI_PREFIX = "/api/v1/auth/admin";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
        response.setHeader(TRACE_ID_HEADER, traceId);

        MDC.put(STORIXStatic.Mdc.TRACE_ID, traceId);
        MDC.put(STORIXStatic.Mdc.ENDPOINT, request.getRequestURI());
        MDC.put(STORIXStatic.Mdc.HTTP_METHOD, request.getMethod());

        boolean admin = isAdminRequest(request.getRequestURI());
        if (admin) {
            log.info(">>> [Admin] 요청 진입 query={}", request.getQueryString());
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (admin) {
                log.info(">>> [Admin] 요청 완료 status={}", response.getStatus());
            }
            MDC.remove(STORIXStatic.Mdc.TRACE_ID);
            MDC.remove(STORIXStatic.Mdc.ENDPOINT);
            MDC.remove(STORIXStatic.Mdc.HTTP_METHOD);
            MDC.remove(STORIXStatic.Mdc.USER_ID);
        }
    }

    private boolean isAdminRequest(String uri) {
        return uri.startsWith(ADMIN_URI_PREFIX) || uri.startsWith(ADMIN_AUTH_URI_PREFIX);
    }

    private String resolveTraceId(String fromHeader) {
        if (fromHeader != null && TRACE_ID_PATTERN.matcher(fromHeader).matches()) {
            return fromHeader;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}
