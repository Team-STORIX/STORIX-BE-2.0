package com.storix.infrastructure.global.security;

import com.storix.common.utils.STORIXStatic;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class MdcContextFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
        response.setHeader(TRACE_ID_HEADER, traceId);

        MDC.put(STORIXStatic.Mdc.TRACE_ID, traceId);
        MDC.put(STORIXStatic.Mdc.ENDPOINT, request.getRequestURI());
        MDC.put(STORIXStatic.Mdc.HTTP_METHOD, request.getMethod());
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(STORIXStatic.Mdc.TRACE_ID);
            MDC.remove(STORIXStatic.Mdc.ENDPOINT);
            MDC.remove(STORIXStatic.Mdc.HTTP_METHOD);
            MDC.remove(STORIXStatic.Mdc.USER_ID);
        }
    }

    private String resolveTraceId(String fromHeader) {
        if (fromHeader != null && TRACE_ID_PATTERN.matcher(fromHeader).matches()) {
            return fromHeader;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}
