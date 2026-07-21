package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.api.domain.user.controller.dto.AuthorizationResponse;
import com.storix.api.domain.user.controller.dto.LoginWithTokenResponse;
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
                yield validateNaver(naverToken.accessToken(), naverToken.refreshToken());
            }
            case X -> {
                XTokenResponse xToken = oauthHelper.getXOAuthToken(req.authCode(), req.redirectUri(), req.codeVerifier());
                yield validateX(xToken.accessToken(), xToken.refreshToken());
            }

            case APPLE, SLACK -> throw UnsupportedOAuthProviderException.EXCEPTION;
        };
    }

    // 독자 회원 가입 가능 여부 (Native)
    // - Kakao/Naver: SDK가 내려준 accessToken(+idToken)으로 검증
    // - Apple/X: 앱이 내려준 authorizationCode로 토큰을 얻은 뒤 공통 검증
    public ValidAuthDTO checkAvailableRegisterNative(OAuthAuthorizationRequest req, OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> validateKakao(req.accessToken(), req.idToken(), true);
            case NAVER -> validateNaver(req.accessToken(), req.refreshToken());
            case APPLE -> {
                AppleTokenResponse appleToken = oauthHelper.getAppleOAuthToken(req.authCode());
                yield validateApple(appleToken.idToken(), appleToken.refreshToken());
            }
            case X -> {
                XTokenResponse xToken = oauthHelper.getXOAuthToken(req.authCode(), req.redirectUri(), req.codeVerifier());
                yield validateX(xToken.accessToken(), xToken.refreshToken());
            }

            case SLACK -> throw UnsupportedOAuthProviderException.EXCEPTION;
        };
    }

    // 독자 유저 정보 등록
    public ResponseEntity<CustomResponse<AuthorizationResponse>> readerSignup(ReaderSignUpData data, String jti) {
        AuthUserDetails userDetails = authService.signUpReaderUser(data, jti);
        LoginWithTokenResponse tokenResponse = tokenGenerateHelper.generateLoginWithToken(userDetails);
        AuthorizationResponse result = AuthorizationResponse.nativeRefresh(
                tokenResponse.accessToken(), tokenResponse.refreshToken());

        return ResponseEntity.ok()
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
        OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(idToken, null, OAuthProvider.KAKAO, isNative);

        // token 간 정보 일치 확인 후 회원가입 여부 반환
        if (!oauthInfo.getOid().equals(kakaoUser.id())) throw UnknownUserException.EXCEPTION;
        return authService.validKakaoSignup(kakaoUser.id(), idToken);
    }

    // Naver 공통 검증 로직
    private ValidAuthDTO validateNaver(String accessToken, String refreshToken) {
        // accessToken으로 사용자 정보 조회
        NaverUserResponse naverUser = oauthHelper.getNaverInformation(accessToken);

        // token 간 정보 일치 확인 후 회원가입 여부 반환
        if (naverUser.id() == null) throw FeignClientServerErrorException.EXCEPTION;
        return authService.validNaverSignup(naverUser.id(), refreshToken);
    }

    // Apple 공통 검증 로직
    // - Apple은 Web/Native 모두 동일한 clientId(서비스 ID) 를 aud 로 사용하므로 isNative 구분 불필요 → false.
    // - refresh_token은 탈퇴 시 연동 해제(revoke)용으로 보관
    private ValidAuthDTO validateApple(String idToken, String refreshToken) {
        OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(idToken, null, OAuthProvider.APPLE, false);
        return authService.validAppleSignup(oauthInfo.getOid(), idToken, refreshToken);
    }

    // X 공통 검증 로직
    // accessToken으로 oid 조회, refresh_token은 탈퇴 revoke용 best-effort 보관
    private ValidAuthDTO validateX(String accessToken, String refreshToken) {
        XUserResponse xUser = oauthHelper.getXInformation(accessToken);

        if (xUser == null || xUser.id() == null) throw FeignClientServerErrorException.EXCEPTION;
        return authService.validXSignup(xUser.id(), refreshToken);
    }

}
