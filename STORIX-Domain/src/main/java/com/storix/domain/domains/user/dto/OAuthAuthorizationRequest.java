package com.storix.domain.domains.user.dto;

public record OAuthAuthorizationRequest(
        String authCode,
        String redirectUri, // kakao (web)
        String state,       // naver (web)
        String accessToken, // native (kakao, naver)
        String idToken      // native (kakao OIDC)
) {
    // Web: Kakao Redirect Uri -> authCode
    public static OAuthAuthorizationRequest forKakao(
            String authCode, String redirectUri
    ) {
        return new OAuthAuthorizationRequest(authCode, redirectUri, null, null, null);
    }

    // Web: Naver Redirect Uri -> authCode + state
    public static OAuthAuthorizationRequest forNaver(
            String authCode, String state
    ) {
        return new OAuthAuthorizationRequest(authCode, null, state, null, null);
    }

    // Native: Apple SDK -> authCode
    public static OAuthAuthorizationRequest forAppleNative(String authCode) {
        return new OAuthAuthorizationRequest(authCode, null, null, null, null);
    }

    // Native: Kakao SDK -> accessToken + idToken
    public static OAuthAuthorizationRequest forKakaoNative(String accessToken, String idToken) {
        return new OAuthAuthorizationRequest(null, null, null, accessToken, idToken);
    }

    // Native: Naver SDK -> accessToken
    public static OAuthAuthorizationRequest forNaverNative(String accessToken) {
        return new OAuthAuthorizationRequest(null, null, null, accessToken, null);
    }
}
