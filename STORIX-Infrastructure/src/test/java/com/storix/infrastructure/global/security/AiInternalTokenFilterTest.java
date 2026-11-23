package com.storix.infrastructure.global.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[보안] AI 서버 토큰 필터")
class AiInternalTokenFilterTest {

    private final AiInternalTokenFilter filter = new AiInternalTokenFilter("ai-token");

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private Authentication run(String uri, String token) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        if (token != null) request.addHeader("X-Internal-Token", token);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    @DisplayName("토큰이 맞으면 AI_INTERNAL 권한을 준다")
    void validToken() throws Exception {
        assertThat(run("/internal/v1/ai/works/enum-catalog", "ai-token").getAuthorities())
                .extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_AI_INTERNAL");
        SecurityContextHolder.clearContext();
        assertThat(run("/internal/v1/ai/users/1/context", "ai-token").getAuthorities())
                .extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_AI_INTERNAL");
    }

    @Test
    @DisplayName("토큰이 틀리거나 없으면 인증하지 않는다")
    void invalidToken() throws Exception {
        assertThat(run("/internal/v1/ai/users/1/context", "wrong")).isNull();
        assertThat(run("/internal/v1/ai/users/1/context", null)).isNull();
    }

    @Test
    @DisplayName("AI 경로가 아니면 토큰이 맞아도 권한을 주지 않는다")
    void nonAiPath() throws Exception {
        assertThat(run("/internal/v1/works/enum-catalog", "ai-token")).isNull();
        assertThat(run("/internal/ai/users/1/context", "ai-token")).isNull();
    }
}
