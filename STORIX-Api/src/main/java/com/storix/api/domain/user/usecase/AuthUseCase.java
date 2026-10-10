package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.user.dto.OnboardingPrincipal;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.api.domain.user.controller.dto.AuthorizationResponse;
import com.storix.api.domain.user.controller.dto.LoginWithTokenResponse;
import com.storix.api.domain.user.helper.TokenGenerateHelper;
import com.storix.domain.domains.user.service.AuthService;
import com.storix.api.domain.user.helper.OAuthHelper;
import com.storix.api.domain.user.helper.OAuthSignupValidator;
import com.storix.domain.domains.user.dto.*;
import com.storix.domain.domains.user.domain.OAuthProvider;
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
    private final OAuthSignupValidator oAuthSignupValidator;
    private final TokenGenerateHelper tokenGenerateHelper;

    // 독자 회원 가입 가능 여부 (Web)
    // - authCode로 토큰을 얻은 뒤 공통 검증
    public ValidAuthDTO checkAvailableRegister(OAuthAuthorizationRequest req, OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> {
                KakaoTokenResponse kakaoToken = oauthHelper.getKakaoOAuthToken(req.authCode(), req.redirectUri());
                yield oAuthSignupValidator.validateKakao(kakaoToken.accessToken(), kakaoToken.idToken(), false);
            }
            case NAVER -> {
                NaverTokenResponse naverToken = oauthHelper.getNaverOAuthToken(req.authCode(), req.state());
                yield oAuthSignupValidator.validateNaver(naverToken.accessToken(), naverToken.refreshToken());
            }
            case X -> {
                XTokenResponse xToken = oauthHelper.getXOAuthToken(req.authCode(), req.redirectUri(), req.codeVerifier());
                yield oAuthSignupValidator.validateX(xToken.accessToken(), xToken.refreshToken());
            }

            case APPLE, SLACK -> throw UnsupportedOAuthProviderException.EXCEPTION;
        };
    }

    // 독자 회원 가입 가능 여부 (Native)
    // - Kakao/Naver: SDK가 내려준 accessToken(+idToken)으로 검증
    // - Apple/X: 앱이 내려준 authorizationCode로 토큰을 얻은 뒤 공통 검증
    public ValidAuthDTO checkAvailableRegisterNative(OAuthAuthorizationRequest req, OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> oAuthSignupValidator.validateKakao(req.accessToken(), req.idToken(), true);
            case NAVER -> oAuthSignupValidator.validateNaver(req.accessToken(), req.refreshToken());
            case APPLE -> {
                AppleTokenResponse appleToken = oauthHelper.getAppleOAuthToken(req.authCode());
                yield oAuthSignupValidator.validateApple(appleToken.idToken(), appleToken.refreshToken());
            }
            case X -> {
                XTokenResponse xToken = oauthHelper.getXOAuthToken(req.authCode(), req.redirectUri(), req.codeVerifier());
                yield oAuthSignupValidator.validateX(xToken.accessToken(), xToken.refreshToken());
            }

            case SLACK -> throw UnsupportedOAuthProviderException.EXCEPTION;
        };
    }

    // 독자 유저 정보 등록
    public ResponseEntity<CustomResponse<AuthorizationResponse>> readerSignup(ReaderSignUpData data, String jti) {
        // Redis 조회·검증은 트랜잭션 밖에서 끝낸다
        OnboardingPrincipal principal = authService.findOnboardingPrincipal(jti);
        authService.checkOnboardingWorks(data.favoriteWorksIdList());

        AuthUserDetails userDetails = authService.signUpReaderUser(data, principal);

        // 가입이 커밋된 뒤에 온보딩 토큰을 지운다. 중간에 실패하면 같은 토큰으로 다시 시도할 수 있어야 한다
        authService.deleteOnboardingToken(jti);

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
}
