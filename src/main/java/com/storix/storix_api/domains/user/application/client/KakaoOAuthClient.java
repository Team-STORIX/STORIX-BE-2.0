package com.storix.storix_api.domains.user.application.client;

import com.storix.storix_api.domains.user.dto.OIDCPublicKeysResponse;
import com.storix.storix_api.domains.user.dto.KakaoTokenResponse;
import com.storix.storix_api.global.config.web.KakaoOauthConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "KakaoOAuthClient",
        url = "https://kauth.kakao.com",
        configuration = KakaoOauthConfig.class
)
public interface KakaoOAuthClient {

    // 인가 코드로 토큰 발급 요청
    @PostMapping("/oauth/token")
    KakaoTokenResponse kakaoAuth(
            @RequestParam("grant_type")String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code,
            @RequestParam("client_secret") String client_secret
    );

    // OIDC 공개키 목록 조회 (인증용)
    @Cacheable(cacheNames = "KakaoOIDC", cacheManager = "oidcCacheManager")
    @GetMapping("/.well-known/jwks.json")
    OIDCPublicKeysResponse getKakaoOIDCOpenKeys();
}