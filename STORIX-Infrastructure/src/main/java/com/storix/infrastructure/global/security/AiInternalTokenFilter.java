package com.storix.infrastructure.global.security;

import com.storix.common.utils.STORIXStatic;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

// AI 서버가 부르는 /internal/v1/ai/** 전용. 토큰이 맞으면 AI_INTERNAL 권한만 준다
// 틀리면 인증 없이 넘겨 SecurityEntryPoint 가 401 로 막는다
@Component
public class AiInternalTokenFilter extends OncePerRequestFilter {

    private final byte[] token;

    public AiInternalTokenFilter(@Value("${internal.ai-token}") String token) {
        this.token = token.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().substring(request.getContextPath().length())
                .startsWith(STORIXStatic.Internal.AI_URI_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(STORIXStatic.Internal.AI_TOKEN_HEADER);
        if (token.length > 0 && header != null && MessageDigest.isEqual(token, header.getBytes(StandardCharsets.UTF_8))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "ai-server", null, List.of(new SimpleGrantedAuthority("ROLE_AI_INTERNAL"))));
        }
        filterChain.doFilter(request, response);
    }
}
