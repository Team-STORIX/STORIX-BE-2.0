package com.storix.domain.domains.user.dto;

public record ValidAuthDTO(
        boolean isRegistered,
        String idToken,           // OIDC provider(Kakao/Apple)만, 그 외 null
        String oid,               // 비OIDC provider(Naver/X)만, 그 외 null
        String oauthRefreshToken  // unlink 정책 있는 provider만, 그 외 null
) {
    // OIDC provider(Kakao/Apple): idToken에서 oid 추출
    public static ValidAuthDTO ofIdToken(boolean isRegistered, String idToken) {
        return new ValidAuthDTO(isRegistered, idToken, null, null);
    }

    // 비OIDC provider(Naver/X): oid 직접 전달
    public static ValidAuthDTO ofOid(boolean isRegistered, String oid, String oauthRefreshToken) {
        return new ValidAuthDTO(isRegistered, null, oid, oauthRefreshToken);
    }
}
