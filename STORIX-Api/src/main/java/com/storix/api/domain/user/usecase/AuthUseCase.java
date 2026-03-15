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

    // 독자 회원 가입 가능 여부
    public ValidAuthDTO checkAvailableRegister(OAuthAuthorizationRequest req, OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> {
                KakaoTokenResponse kakaoToken = oauthHelper.getKakaoOAuthToken(req.authCode(), req.redirectUri());
                KakaoUserResponse kakaoUser = oauthHelper.getKakaoInformation(kakaoToken.accessToken());
                OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(kakaoToken.idToken(), OAuthProvider.KAKAO);
                if (!oauthInfo.getOid().equals(kakaoUser.id())) throw UnknownUserException.EXCEPTION;
                yield authService.validKakaoSignup(kakaoUser.id(), kakaoToken.idToken());
            }
            case NAVER -> {
                NaverTokenResponse naverToken = oauthHelper.getNaverOAuthToken(req.authCode(), req.state());
                NaverUserResponse naverUser = oauthHelper.getNaverInformation(naverToken.accessToken());
                if (naverUser.id() == null) throw FeignClientServerErrorException.EXCEPTION;
                yield authService.validNaverSignup(naverUser.id());
            }
        };
    }

    // 독자 유저 정보 등록
    public ResponseEntity<CustomResponse<AuthorizationResponse>> readerSignup(ReaderSignupRequest req, String jti) {
        AuthUserDetails userDetails = authService.signUpReaderUser(req, jti);
        LoginWithTokenResponse tokenResponse = tokenGenerateHelper.generateLoginWithToken(userDetails);
        AuthorizationResponse result = new AuthorizationResponse(tokenResponse.accessToken());

        return ResponseEntity.ok()
                .headers(cookieHelper.getTokenCookie(tokenResponse.refreshToken()))
                .body(CustomResponse.onSuccess(SuccessCode.AUTH_SIGNUP_SUCCESS, result));
    }

    // 닉네임 중복 체크
    public CustomResponse<Void> checkAvailableNickname(String nickName) {
        authService.validNickname(nickName);
        return CustomResponse.onSuccess(SuccessCode.AUTH_NICKNAME_SUCCESS);
    }

}
