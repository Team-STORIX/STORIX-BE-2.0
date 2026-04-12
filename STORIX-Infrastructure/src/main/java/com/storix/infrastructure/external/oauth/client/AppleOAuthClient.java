package com.storix.infrastructure.external.oauth.client;

import com.storix.domain.domains.user.dto.AppleTokenResponse;
import com.storix.domain.domains.user.dto.OIDCPublicKeysResponse;
import com.storix.infrastructure.external.oauth.config.AppleOauthConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "AppleOAuthClient",
        url = "https://appleid.apple.com",
        configuration = AppleOauthConfig.class
)
public interface AppleOAuthClient {

    // 인가 코드로 토큰 발급 요청 (네이티브: redirect_uri 불필요)
    @PostMapping("/auth/token")
    AppleTokenResponse appleAuth(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String code
    );

    // OIDC 공개키 목록 조회 (id_token 서명 검증용)
    @Cacheable(cacheNames = "AppleOIDC", cacheManager = "oidcCacheManager")
    @GetMapping("/auth/keys")
    OIDCPublicKeysResponse getAppleOIDCOpenKeys();
}
