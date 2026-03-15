package com.storix.api.domain.user.helper;

import com.storix.domain.domains.user.adaptor.JwtOIDCProvider;
import com.storix.common.property.OAuthProperties;
import com.storix.infrastructure.external.oauth.client.KakaoInfoClient;
import com.storix.infrastructure.external.oauth.client.KakaoOAuthClient;
import com.storix.infrastructure.external.oauth.client.NaverInfoClient;
import com.storix.infrastructure.external.oauth.client.NaverOAuthClient;
import com.storix.domain.domains.user.dto.*;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.exception.oauth.OidcJwksRefreshRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import static com.storix.common.utils.STORIXStatic.BEARER;

@Service
@RequiredArgsConstructor
public class OAuthHelper {

    private final OAuthProperties oauthProperties;
    private final JwtOIDCProvider jwtOIDCProvider;
    private final CacheManager oidcCacheManager;

    // 카카오
    private final KakaoOAuthClient kakaoOauthClient;
    private final KakaoInfoClient kakaoInfoClient;

    // 네이버
    private final NaverOAuthClient naverOauthClient;
    private final NaverInfoClient naverInfoClient;

    // 카카오: 인가 코드로 토큰 발급 요청 -> accessToken, idToken
    public KakaoTokenResponse getKakaoOAuthToken(String code, String redirectUri) {
        var kakao = oauthProperties.getKakao();
        return kakaoOauthClient.kakaoAuth(
                "authorization_code",
                kakao.getClientId(),
                redirectUri,
                code,
                kakao.getClientSecret()
        );
    }

    // 카카오: 사용자 정보 요청 (accessToken)
    public KakaoUserResponse getKakaoInformation(String accessToken) {
        return kakaoInfoClient.getUserInfo(BEARER + accessToken);
    }

    // 카카오: 사용자 연결 해제
    public void unlinkKakaoUser(String oid) {
        var kakao = oauthProperties.getKakao();
        String header = "KakaoAK " + kakao.getAdminKey();
        kakaoInfoClient.unlinkUser(
                header,
                "user_id",
                Long.valueOf(oid)
        );
    }

    // 네이버: 인가 코드로 토큰 발급 요청 -> accessToken
    public NaverTokenResponse getNaverOAuthToken(String code, String state) {
        var naver = oauthProperties.getNaver();
        return naverOauthClient.naverAuth(
                "authorization_code",
                naver.getClientId(),
                naver.getClientSecret(),
                code,
                state
        );
    }

    // 네이버: 사용자 정보 요청 (accessToken)
    public NaverUserResponse getNaverInformation(String accessToken) {
        return naverInfoClient.getUserInfo(BEARER + accessToken).response();
    }

    // 네이버: 사용자 연결 해제
    public void unlinkNaverUser(String oid) {
        var naver = oauthProperties.getNaver();
        // 네이버는 토큰 캐싱 및 주기적 갱신 필요
    }


    // OIDC 스펙: OIDC 공개키 목록 조회
    public OIDCPublicKeysResponse getOIDCPublicKeys(OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> kakaoOauthClient.getKakaoOIDCOpenKeys();
            case NAVER -> naverOauthClient.getNaverOIDCOpenKeys();
        };
    }

    // OIDC 스펙: OIDC 공개키 캐시 무효화
    public void clearOidcCacheKeys(OAuthProvider provider) {
        Cache cache;
        switch (provider) {
            case KAKAO -> cache = oidcCacheManager.getCache("KakaoOIDC");
            case NAVER -> cache = oidcCacheManager.getCache("NaverOIDC");
            default -> cache = null;
        }

        if (cache != null) cache.clear();
    }

    // OIDC 스펙: OIDC Config 정보 조회 (baseUrl, clientId)
    public OIDCConfigDTO getOIDCConfig(OAuthProvider provider) {
        switch (provider) {
            case KAKAO -> {
                var kakao = oauthProperties.getKakao();
                return new OIDCConfigDTO(kakao.getBaseUri(), kakao.getClientId());
            }
             case NAVER ->  {
                var naver = oauthProperties.getNaver();
                return new OIDCConfigDTO(naver.getBaseUri(), naver.getClientId());
             }
            default -> {
                return null;
            }
        }
    }

    // OIDC 스펙: idToken 검증 후 OIDC Payload 반환 (iss, aud, sub)
    public OIDCDecodePayload getOIDCDecodePayload(String idToken, OAuthProvider provider) {

        OIDCConfigDTO oidcConfig = getOIDCConfig(provider);
        OIDCPublicKeysResponse oidcPublicKeysResponse = getOIDCPublicKeys(provider);

        try {
            return jwtOIDCProvider.getPayloadFromIdToken(idToken, oidcConfig.baseUri(), oidcConfig.clientId(), oidcPublicKeysResponse);
        } catch (OidcJwksRefreshRequiredException e) {
            clearOidcCacheKeys(provider);
            OIDCPublicKeysResponse newOidcPublicKeysResponse = getOIDCPublicKeys(provider);
            return jwtOIDCProvider.getPayloadFromIdToken(idToken, oidcConfig.baseUri(), oidcConfig.clientId(), newOidcPublicKeysResponse);
        }
    }

    // OIDC 스펙: 검증된 idToken으로 OAuthInfo 반환 (provider, oid)
    public OAuthInfo getOauthInfoByIdToken(String idToken, OAuthProvider provider) {
        if (provider == OAuthProvider.NAVER) {
            return OAuthInfo.builder()
                    .provider(provider)
                    .oid(idToken) // 네이버인 경우, 일시적으로 idToken 값에 oid 값 반환
                    .build();
        }
        OIDCDecodePayload oidcDecodePayload = getOIDCDecodePayload(idToken, provider);
        return OAuthInfo.builder()
                .provider(provider)
                .oid(oidcDecodePayload.sub())
                .build();
    }

}
