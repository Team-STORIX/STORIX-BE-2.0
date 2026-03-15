package com.storix.storix_api.domains.user.controller.dto;

public record OAuthAuthorizationRequest(
        String authCode,
        String redirectUri, // kakao
        String state // naver
) {
    public static OAuthAuthorizationRequest forKakao(
            String authCode, String redirectUri
    ) {
        return new OAuthAuthorizationRequest(authCode, redirectUri, null);
    }

    public static OAuthAuthorizationRequest forNaver(
            String authCode, String state
    ) {
        return new OAuthAuthorizationRequest(authCode, null, state);
    }
}