package com.storix.infrastructure.global.security;

import com.storix.domain.domains.user.adaptor.OnboardingUserDetails;
import com.storix.domain.domains.user.adaptor.TokenAdaptor;
import com.storix.domain.domains.user.dto.OnboardingPrincipal;
import com.storix.infrastructure.global.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.storix.common.utils.STORIXStatic.BEARER;

@Component
@RequiredArgsConstructor
public class OnboardingAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final TokenAdaptor tokenAdaptor;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("/api/v1/auth/users/reader/signup".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            Authentication authentication = getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public Authentication getAuthentication(String token) {
        String jti = tokenProvider.parseOnboardingToken(token);
        OnboardingPrincipal principal = tokenAdaptor.findOnboardingPrincipalByJti(jti);

        OnboardingUserDetails userDetails = new OnboardingUserDetails(
                jti, principal.provider(), principal.oid());
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, List.of());
    }
}