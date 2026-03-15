package com.storix.api.domain.user.helper;

import com.storix.infrastructure.global.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CookieHelper {

    private final TokenProvider tokenProvider;

    private final Environment environment;

    public boolean isLocal() {
        // Spring Profiles: local(로컬 개발), dev(개발 서버), staging(스테이징), prod(운영 서버)
        return environment.acceptsProfiles(Profiles.of("local"));
    }

    public HttpHeaders getTokenCookie(String refreshToken) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder =
                ResponseCookie.from("refreshToken", refreshToken)
                        .httpOnly(true)
                        .secure(!isLocal())
                        .sameSite(isLocal() ? "Lax" : "Strict")
                        .path("/")
                        .maxAge(Duration.ofMillis(tokenProvider.getRefreshTokenValidityMs()));

        if (!isLocal()) {
            cookieBuilder.domain(".storix.kr");
        }

        ResponseCookie refreshTokenCookie = cookieBuilder.build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        return httpHeaders;
    }

    public HttpHeaders deleteCookie() {
        ResponseCookie.ResponseCookieBuilder cookieBuilder =
                ResponseCookie.from("refreshToken", null)
                        .httpOnly(true)
                        .secure(!isLocal())
                        .sameSite(isLocal() ? "Lax" : "Strict")
                        .path("/")
                        .maxAge(0);

        if (!isLocal()) {
            cookieBuilder.domain(".storix.kr");
        }

        ResponseCookie refreshTokenCookie = cookieBuilder.build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        return httpHeaders;
    }
}
