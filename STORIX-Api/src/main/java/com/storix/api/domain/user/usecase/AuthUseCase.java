package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.api.domain.user.controller.dto.AuthorizationResponse;
import com.storix.api.domain.user.controller.dto.LoginWithTokenResponse;
import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.api.domain.user.helper.TokenGenerateHelper;
import com.storix.domain.domains.user.service.AuthService;
import com.storix.api.domain.user.helper.OAuthHelper;
import com.storix.domain.domains.user.dto.*;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.exception.me.UnknownUserException;
import com.storix.domain.domains.user.exception.oauth.FeignClientServerErrorException;
import com.storix.domain.domains.user.exception.oauth.UnsupportedOAuthProviderException;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@UseCase
@RequiredArgsConstructor
public class AuthUseCase {

    private final AuthService authService;

    private final OAuthHelper oauthHelper;
    private final TokenGenerateHelper tokenGenerateHelper;
    private final CookieHelper cookieHelper;

    // 독자 회원 가입 가능 여부 (Web)
    // - authCode로 토큰을 얻은 뒤 공통 검증
    public ValidAuthDTO checkAvailableRegister(OAuthAuthorizationRequest req, OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> {
                KakaoTokenResponse kakaoToken = oauthHelper.getKakaoOAuthToken(req.authCode(), req.redirectUri());
                yield validateKakao(kakaoToken.accessToken(), kakaoToken.idToken(), false);
            }
            case NAVER -> {
                NaverTokenResponse naverToken = oauthHelper.getNaverOAuthToken(req.authCode(), req.state());
                yield validateNaver(naverToken.accessToken());
            }

            case APPLE, SLACK -> throw UnsupportedOAuthProviderException.EXCEPTION;
        };
    }

    // 독자 회원 가입 가능 여부 (Native)
    // - Kakao/Naver: SDK가 내려준 accessToken(+idToken)으로 검증
    // - Apple: SDK가 내려준 authorizationCode로 토큰을 얻은 뒤 공통 검증
    public ValidAuthDTO checkAvailableRegisterNative(OAuthAuthorizationRequest req, OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> validateKakao(req.accessToken(), req.idToken(), true);
            case NAVER -> validateNaver(req.accessToken());
            case APPLE -> {
                AppleTokenResponse appleToken = oauthHelper.getAppleOAuthToken(req.authCode());
                yield validateApple(appleToken.idToken());
            }

            case SLACK -> throw UnsupportedOAuthProviderException.EXCEPTION;
        };
    }

    // 독자 유저 정보 등록
    public ResponseEntity<CustomResponse<AuthorizationResponse>> readerSignup(ReaderSignUpData data, String jti) {
        AuthUserDetails userDetails = authService.signUpReaderUser(data, jti);
        LoginWithTokenResponse tokenResponse = tokenGenerateHelper.generateLoginWithToken(userDetails);
        AuthorizationResponse result = AuthorizationResponse.webRefresh(tokenResponse.accessToken());

        return ResponseEntity.ok()
                .headers(cookieHelper.getTokenCookie(tokenResponse.refreshToken()))
                .body(CustomResponse.onSuccess(SuccessCode.AUTH_SIGNUP_SUCCESS, result));
    }

    // 닉네임 중복 체크
    public CustomResponse<Void> checkAvailableNickname(String nickName) {
        authService.validNickname(nickName);
        return CustomResponse.onSuccess(SuccessCode.AUTH_NICKNAME_SUCCESS);
    }



    // Kakao 공통 검증 로직
    // - isNative=true 이면 idToken의 aud(audience) 검증을 Native App Key 로 수행
    //   (Kakao는 Web=REST API Key / Native=Native App Key 로 idToken.aud 가 다름)
    private ValidAuthDTO validateKakao(String accessToken, String idToken, boolean isNative) {
        // accessToken으로 사용자 정보 조회
        KakaoUserResponse kakaoUser = oauthHelper.getKakaoInformation(accessToken);
        // idToken으로 OIDC 검증
        OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(idToken, OAuthProvider.KAKAO, isNative);

        // token 간 정보 일치 확인 후 회원가입 여부 반환
        if (!oauthInfo.getOid().equals(kakaoUser.id())) throw UnknownUserException.EXCEPTION;
        return authService.validKakaoSignup(kakaoUser.id(), idToken);
    }

    // Naver 공통 검증 로직
    private ValidAuthDTO validateNaver(String accessToken) {
        // accessToken으로 사용자 정보 조회
        NaverUserResponse naverUser = oauthHelper.getNaverInformation(accessToken);

        // token 간 정보 일치 확인 후 회원가입 여부 반환
        if (naverUser.id() == null) throw FeignClientServerErrorException.EXCEPTION;
        return authService.validNaverSignup(naverUser.id());
    }

    // Apple 공통 검증 로직
    // - Apple은 Web/Native 모두 동일한 clientId(서비스 ID) 를 aud 로 사용하므로 isNative 구분 불필요 → false.
    private ValidAuthDTO validateApple(String idToken) {
        OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(idToken, OAuthProvider.APPLE, false);
        return authService.validAppleSignup(oauthInfo.getOid(), idToken);
    }

}
