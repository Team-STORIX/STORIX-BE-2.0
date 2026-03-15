package com.storix.storix_api.global.security;

import com.storix.storix_api.domains.user.dto.OnboardingTokenInfo;
import com.storix.storix_api.global.apiPayload.exception.user.ExpiredOnboardingTokenException;
import com.storix.storix_api.global.apiPayload.exception.user.ExpiredRefreshTokenException;
import com.storix.storix_api.global.apiPayload.exception.user.ExpiredTokenException;
import com.storix.storix_api.global.apiPayload.exception.user.InvalidTokenException;
import com.storix.storix_api.global.security.dto.AccessTokenInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import static com.storix.storix_api.global.apiPayload.STORIXStatic.*;

@Component
@RequiredArgsConstructor
public class TokenProvider implements InitializingBean {

    @Value("${JWT_SECRET_KEY}") private String secretKey;
    @Value("${JWT_ACCESS_TOKEN_VALIDITY_MS}") private long accessTokenValidityMs;
    @Value(("${JWT_REFRESH_TOKEN_VALIDITY_MS}")) private long refreshTokenValidityMs;
    @Value(("${JWT_ONBOARDING_TOKEN_VALIDITY_MS}")) private long onboardingTokenValidityMs;


    private Key key;

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes); // HMAC-SHA
    }

    private Jws<Claims> getJws(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw ExpiredTokenException.EXCEPTION;
        } catch (Exception e) {
            throw InvalidTokenException.EXCEPTION;
        }
    }

    public String createAccessToken(Long userId, String role) {

        final Date issuedAt = new Date();
        final Date expiredAt = new Date(issuedAt.getTime() + accessTokenValidityMs);

        return Jwts.builder()
                .setIssuer(TOKEN_ISSUR)
                .setSubject(userId.toString())
                .claim(TOKEN_TYPE, ACCESS_TOKEN)
                .claim(TOKEN_ROLE, role)
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId) {

        final Date issuedAt = new Date();
        final Date expiredAt = new Date(issuedAt.getTime() + refreshTokenValidityMs);

        return Jwts.builder()
                .setIssuer(TOKEN_ISSUR)
                .setSubject(userId.toString())
                .claim(TOKEN_TYPE, REFRESH_TOKEN)
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public OnboardingTokenInfo createOnboardingToken() {

        String jti = UUID.randomUUID().toString();

        final Date issuedAt = new Date();
        final Date expiredAt = new Date(issuedAt.getTime() + onboardingTokenValidityMs);

        String onboardingToken = Jwts.builder()
                .setIssuer(TOKEN_ISSUR)
                .setSubject(jti)
                .claim(TOKEN_TYPE, ONBOARDING_TOKEN)
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new OnboardingTokenInfo(onboardingToken, jti);
    }

    public boolean isAccessToken(String token) {
        return getJws(token).getBody().get(TOKEN_TYPE).equals(ACCESS_TOKEN);
    }

    public boolean isRefreshToken(String token) { return getJws(token).getBody().get(TOKEN_TYPE).equals(REFRESH_TOKEN); }

    public boolean isOnboardingToken(String token) { return getJws(token).getBody().get(TOKEN_TYPE).equals(ONBOARDING_TOKEN); }

    public AccessTokenInfo parseAccessToken(String token) {
        if (isAccessToken(token)) {
            Claims claims = getJws(token).getBody();
            return AccessTokenInfo.builder()
                    .userId(Long.parseLong(claims.getSubject()))
                    .role((String) claims.get(TOKEN_ROLE))
                    .build();
        }
        throw InvalidTokenException.EXCEPTION;
    }

    public Long parseRefreshToken(String token) {
        try {
            if (isRefreshToken(token)) {
                Claims claims = getJws(token).getBody();
                return Long.parseLong(claims.getSubject());
            }
        } catch (ExpiredTokenException e) {
            throw ExpiredRefreshTokenException.EXCEPTION;
        }
        throw InvalidTokenException.EXCEPTION;
    }

    public String parseOnboardingToken(String token) {
        try {
            if (isOnboardingToken(token)) {
                Claims claims = getJws(token).getBody();
                return claims.getSubject();
            }
        } catch (ExpiredTokenException e) {
            throw ExpiredOnboardingTokenException.EXCEPTION;
        }
        throw InvalidTokenException.EXCEPTION;
    }

    public Long getRefreshTokenValidityMs() { return refreshTokenValidityMs; }

    public Long getOnboardingTokenValidityMs() { return onboardingTokenValidityMs; }

}
