package com.storix.infrastructure.global.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[요청 제한] 정책 선택")
class RateLimitPolicyTest {

    @Test
    @DisplayName("로그인 · 토큰 재발급은 메서드와 관계없이 AUTH")
    void auth() {
        assertThat(RateLimitPolicy.of("POST", "/api/v1/auth/oauth/kakao/login")).isEqualTo(RateLimitPolicy.AUTH);
        assertThat(RateLimitPolicy.of("POST", "/api/v1/auth/tokens/refresh")).isEqualTo(RateLimitPolicy.AUTH);
        assertThat(RateLimitPolicy.of("GET", "/api/v1/auth/oauth/naver/login")).isEqualTo(RateLimitPolicy.AUTH);
    }

    @Test
    @DisplayName("GET · HEAD 가 아니면 WRITE")
    void write() {
        assertThat(RateLimitPolicy.of("POST", "/api/v1/plus/reader/board")).isEqualTo(RateLimitPolicy.WRITE);
        assertThat(RateLimitPolicy.of("DELETE", "/api/v1/feed/reader/board/1")).isEqualTo(RateLimitPolicy.WRITE);
    }

    @Test
    @DisplayName("검색 경로 GET 은 SEARCH, 나머지 GET 은 DEFAULT")
    void searchAndDefault() {
        assertThat(RateLimitPolicy.of("GET", "/api/v2/search/works")).isEqualTo(RateLimitPolicy.SEARCH);
        assertThat(RateLimitPolicy.of("GET", "/api/v1/library/search/works")).isEqualTo(RateLimitPolicy.SEARCH);
        assertThat(RateLimitPolicy.of("GET", "/api/v1/works/1")).isEqualTo(RateLimitPolicy.DEFAULT);
    }
}
