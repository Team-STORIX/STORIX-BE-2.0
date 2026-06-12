package com.storix.infrastructure.external.oauth.client;

import com.storix.domain.domains.user.dto.NaverTokenResponse;
import com.storix.domain.domains.user.dto.OIDCPublicKeysResponse;
import com.storix.infrastructure.external.oauth.config.NaverOauthConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "NaverOAuthClient",
        url = "https://nid.naver.com",
        configuration = NaverOauthConfig.class
)
public interface NaverOAuthClient {

    // 인가 코드로 토큰 발급 요청
    @PostMapping("/oauth2.0/token")
    NaverTokenResponse naverAuth(
            @RequestParam("grant_type")String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String code,
            @RequestParam("state") String state
    );

    // refresh_token으로 access_token 재발급
    @PostMapping("/oauth2.0/token")
    NaverTokenResponse naverRefresh(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("refresh_token") String refreshToken
    );

    // 연동 해제 (회원 탈퇴)
    @PostMapping("/oauth2.0/token")
    void naverDelete(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("access_token") String accessToken,
            @RequestParam("service_provider") String serviceProvider
    );

    // OIDC 공개키 목록 조회 (인증용)
    @Cacheable(cacheNames = "NaverOIDC", cacheManager = "oidcCacheManager")
    @GetMapping("/jwks")
    OIDCPublicKeysResponse getNaverOIDCOpenKeys();
}