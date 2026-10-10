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

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final byte[] apiKey;

    public InternalApiKeyFilter(@Value("${internal.api-key}") String apiKey) {
        this.apiKey = apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(STORIXStatic.Internal.URI_PREFIX);
    }

    // 키가 맞으면 INTERNAL 권한만 준다. 틀리면 인증 없이 넘겨 SecurityEntryPoint 가 401 로 막음
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(STORIXStatic.Internal.API_KEY_HEADER);
        if (apiKey.length > 0 && header != null && MessageDigest.isEqual(apiKey, header.getBytes(StandardCharsets.UTF_8))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "internal", null, List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))));
        }
        filterChain.doFilter(request, response);
    }
}
