package com.storix.infrastructure.external.oauth.client;

import com.storix.domain.domains.user.dto.XTokenResponse;
import com.storix.infrastructure.external.oauth.config.XOauthConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "XOAuthClient",
        url = "https://api.x.com",
        configuration = XOauthConfig.class
)
public interface XOAuthClient {

    // 인가 코드로 토큰 발급 (PKCE, Basic 인증)
    @PostMapping("/2/oauth2/token")
    XTokenResponse xAuth(
            @RequestHeader("Authorization") String basicAuth,
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code,
            @RequestParam("code_verifier") String codeVerifier
    );

    // 토큰 폐기 (탈퇴 시 연동 해제, Basic 인증)
    @PostMapping("/2/oauth2/revoke")
    void xRevoke(
            @RequestHeader("Authorization") String basicAuth,
            @RequestParam("token") String token,
            @RequestParam("token_type_hint") String tokenTypeHint,
            @RequestParam("client_id") String clientId
    );
}
