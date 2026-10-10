package com.storix.infrastructure.global.ratelimit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum RateLimitPolicy {

    AUTH(30),
    WRITE(60),
    SEARCH(120),
    DEFAULT(600);

    private static final List<String> AUTH_URI_PREFIXES = List.of(
            "/api/v1/auth/oauth/",
            "/api/v1/auth/tokens/refresh",
            "/api/v1/auth/tester/login",
            "/api/v1/auth/tester/signup",
            "/api/v1/auth/admin/login",
            "/api/v1/auth/admin/signup"
    );

    private final int limitPerMinute;

    // 앞에서부터 먼저 걸리는 정책 하나만 적용
    public static RateLimitPolicy of(String method, String uri) {
        if (AUTH_URI_PREFIXES.stream().anyMatch(uri::startsWith)) return AUTH;
        if (!HttpMethod.GET.matches(method) && !HttpMethod.HEAD.matches(method)) return WRITE;
        if (uri.contains("/search")) return SEARCH;
        return DEFAULT;
    }
}
