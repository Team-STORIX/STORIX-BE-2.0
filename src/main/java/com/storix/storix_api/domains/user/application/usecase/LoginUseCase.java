package com.storix.storix_api.domains.user.application.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.domains.user.application.usecase.helper.CookieHelper;
import com.storix.storix_api.domains.user.application.usecase.helper.TokenGenerateHelper;
import com.storix.storix_api.domains.user.application.service.ArtistLoginService;
import com.storix.storix_api.domains.user.application.service.ReaderLoginService;
import com.storix.storix_api.domains.user.controller.dto.*;
import com.storix.storix_api.domains.user.domain.OAuthInfo;
import com.storix.storix_api.domains.user.domain.OAuthProvider;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@UseCase
@RequiredArgsConstructor
public class LoginUseCase {

    private final ReaderLoginService readerLoginService;
    private final ArtistLoginService artistLoginService;
    private final TokenGenerateHelper tokenGenerateHelper;

    private final CookieHelper cookieHelper;

    /**UserDetails -> AuthUserDetails: (String)userId, (String)role */

    /**
     * 독자용
     * */
    // 회원가입한 경우 로그인
    public ResponseEntity<CustomResponse<ReaderSocialLoginResponse>> readerLoginWithIdToken(String idToken, OAuthProvider provider) {
        AuthUserDetails userDetails = readerLoginService.execute(idToken, provider);
        LoginWithTokenResponse loginToken = tokenGenerateHelper.generateLoginWithToken(userDetails);

        ReaderLoginResponse readerLoginResponse = new ReaderLoginResponse(
                loginToken.accessToken()
        );

        return ResponseEntity.ok()
                .headers(cookieHelper.getTokenCookie(loginToken.refreshToken()))
                .body(CustomResponse.onSuccess(SuccessCode.OAUTH_LOGIN_SUCCESS,
                        new ReaderSocialLoginResponse(true, readerLoginResponse, null)));
    }

    // 회원가입하지 않은 경우 로그인
    public ResponseEntity<CustomResponse<ReaderSocialLoginResponse>> readerPreLoginWithIdToken(String idToken, OAuthProvider provider) {
        OAuthInfo oauthInfo = readerLoginService.getOauthInfoByIdToken(idToken, provider);

        OAuthLoginWithTokenResponse onboardingToken = tokenGenerateHelper.generateOAuthLoginWithToken(oauthInfo);

        ReaderPreLoginResponse readerPreLoginResponse = new ReaderPreLoginResponse(
                onboardingToken.onboardingToken()
        );

        return ResponseEntity.ok()
                .body(CustomResponse.onSuccess(SuccessCode.OAUTH_PRE_LOGIN_SUCCESS,
                        new ReaderSocialLoginResponse(false, null, readerPreLoginResponse)));
    }

    /**
     * 작가용
     * username = loginId
     * */
    // 로그인
    public ResponseEntity<CustomResponse<AuthorizationResponse>> artistLoginWithLoginId(ArtistLoginRequest req) {
        artistLoginService.validateArtistLogin(req.loginId(), req.password());

        AuthUserDetails userDetails = artistLoginService.loadUserByUsername(req.loginId());
        LoginWithTokenResponse tokenResponse = tokenGenerateHelper.generateLoginWithToken(userDetails);
        AuthorizationResponse result = new AuthorizationResponse(tokenResponse.accessToken());

        return ResponseEntity.ok()
                .headers(cookieHelper.getTokenCookie(tokenResponse.refreshToken()))
                .body(CustomResponse.onSuccess(SuccessCode.AUTH_ARTIST_LOGIN_SUCCESS, result));
    }

}
