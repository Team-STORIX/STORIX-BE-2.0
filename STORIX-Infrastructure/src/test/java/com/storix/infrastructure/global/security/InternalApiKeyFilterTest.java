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

@DisplayName("[보안] 내부 API 키 필터")
class InternalApiKeyFilterTest {

    private final InternalApiKeyFilter filter = new InternalApiKeyFilter("secret-key");

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private Authentication run(String uri, String key) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        if (key != null) request.addHeader("X-Internal-Api-Key", key);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    @DisplayName("키가 맞으면 INTERNAL 권한을 준다")
    void validKey() throws Exception {
        Authentication authentication = run("/internal/v1/works/import", "secret-key");

        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_INTERNAL");
    }

    @Test
    @DisplayName("키가 틀리거나 없으면 인증하지 않는다")
    void invalidKey() throws Exception {
        assertThat(run("/internal/v1/works/import", "wrong")).isNull();
        assertThat(run("/internal/v1/works/import", null)).isNull();
    }

    @Test
    @DisplayName("내부 경로가 아니면 키가 맞아도 권한을 주지 않는다")
    void nonInternalPath() throws Exception {
        assertThat(run("/api/v1/admin/works/import", "secret-key")).isNull();
    }

    @Test
    @DisplayName("context-path 가 붙어도 내부 경로로 판정한다")
    void withContextPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/storix/internal/v1/works/import");
        request.setContextPath("/storix");
        request.addHeader("X-Internal-Api-Key", "secret-key");
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("키 설정이 비어 있으면 빈 헤더로 통과시키지 않는다")
    void emptyConfiguredKey() throws Exception {
        InternalApiKeyFilter emptyKeyFilter = new InternalApiKeyFilter("");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/internal/v1/works/import");
        request.addHeader("X-Internal-Api-Key", "");
        emptyKeyFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
