package com.storix.infrastructure.global.ratelimit;

import com.storix.common.utils.RedisKeyStatic;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.infrastructure.global.ratelimit.exception.TooManyRequestsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_SECONDS = 60;
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final List<String> EXCLUDED_URI_PREFIXES = List.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/ws-stomp",
            "/favicon.ico",
            STORIXStatic.Internal.URI_PREFIX
    );

    private final RateLimitCounter rateLimitCounter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return HttpMethod.OPTIONS.matches(request.getMethod())
                || EXCLUDED_URI_PREFIXES.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 1. 정책 · 대상 결정: 로그인 사용자는 userId, 아니면 IP
        RateLimitPolicy policy = RateLimitPolicy.of(request.getMethod(), request.getRequestURI());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String subject = authentication != null && authentication.getPrincipal() instanceof AuthUserDetails user
                ? "user:" + user.getUserId()
                : "ip:" + clientIp(request);

        // 2. 1분 구간 횟수 집계
        long now = Instant.now().getEpochSecond();
        String key = RedisKeyStatic.RateLimit.PREFIX + policy.name() + ":" + subject + ":" + now / WINDOW_SECONDS;
        long count = rateLimitCounter.increment(key, WINDOW_SECONDS);

        // 3. 초과 시 429. 로그는 구간마다 처음 넘었을 때만
        if (count > policy.getLimitPerMinute()) {
            if (count == policy.getLimitPerMinute() + 1) {
                log.atWarn()
                        .addKeyValue("policy", policy.name())
                        .addKeyValue("subject", subject)
                        .addKeyValue("limit", policy.getLimitPerMinute())
                        .log(">>> [RateLimit] 요청 횟수 초과");
            }
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(WINDOW_SECONDS - now % WINDOW_SECONDS));
            throw TooManyRequestsException.EXCEPTION;
        }
        filterChain.doFilter(request, response);
    }

    // ALB 가 실제 접속 IP 를 X-Forwarded-For 맨 뒤에 붙임. 앞쪽 값은 클라이언트가 위조 가능
    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (!StringUtils.hasText(forwardedFor)) return request.getRemoteAddr();
        String[] ips = forwardedFor.split(",");
        return ips[ips.length - 1].trim();
    }
}
