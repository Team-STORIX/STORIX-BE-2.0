package com.storix.infrastructure.global.security;

import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.adaptor.UserBlacklistAdaptor;
import com.storix.domain.domains.user.domain.Role;
import com.storix.domain.domains.user.domain.UserBlacklist.BlockReason;
import com.storix.domain.domains.user.exception.auth.AlreadyWithDrawUserException;
import com.storix.domain.domains.user.exception.auth.SuspendedUserException;
import com.storix.infrastructure.global.TokenProvider;
import com.storix.infrastructure.global.dto.AccessTokenInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.storix.common.utils.STORIXStatic.BEARER;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final UserBlacklistAdaptor userBlacklistAdaptor;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return uri.startsWith("/api/v1/auth/oauth/")
                || uri.equals("/api/v1/auth/nickname/valid")
                || uri.equals("/api/v1/auth/users/reader/signup")
                || uri.equals("/api/v1/auth/tokens/refresh")
                || uri.equals("/api/v1/auth/developer/signup")
                || uri.equals("/api/v1/auth/developer/login")
                || uri.equals("/api/v1/auth/developer/slack/callback")
                || uri.startsWith("/api/v1/onboarding/");
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
        AccessTokenInfo accessTokenInfo = tokenProvider.parseAccessToken(token);

        getBlockReasonSafely(accessTokenInfo.userId()).ifPresent(reason -> {
            if (reason == BlockReason.SUSPENDED) throw SuspendedUserException.EXCEPTION;
            if (reason == BlockReason.DELETED) throw AlreadyWithDrawUserException.EXCEPTION;
        });

        AuthUserDetails userDetails = new AuthUserDetails(
                accessTokenInfo.userId(), Role.fromValue(accessTokenInfo.role()));
        return new UsernamePasswordAuthenticationToken(
                userDetails, "user", userDetails.getAuthorities());
    }

    // Redis 장애 시 인증 전체가 막히지 않도록 fail-open 처리
    private Optional<BlockReason> getBlockReasonSafely(Long userId) {
        try {
            return userBlacklistAdaptor.getBlockReason(userId);
        } catch (Exception e) {
            log.warn("Failed to check user blacklist status for userId={}", userId, e);
            return Optional.empty();
        }
    }
}