package com.storix.api.domain.user.usecase;

import com.storix.api.domain.user.controller.dto.LoginWithTokenResponse;
import com.storix.api.domain.user.controller.dto.ReaderLoginResponse;
import com.storix.api.domain.user.controller.dto.ReaderPreLoginResponse;
import com.storix.api.domain.user.controller.dto.ReaderSocialLoginResponse;
import com.storix.api.domain.user.controller.dto.OAuthLoginWithTokenResponse;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.api.domain.user.helper.TokenGenerateHelper;
import com.storix.api.domain.user.helper.OAuthHelper;
import com.storix.domain.domains.user.service.ReaderLoginService;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@UseCase
@RequiredArgsConstructor
public class LoginUseCase {

    private final ReaderLoginService readerLoginService;

    private final OAuthHelper oauthHelper;
    private final TokenGenerateHelper tokenGenerateHelper;
    private final CookieHelper cookieHelper;

    /**UserDetails -> AuthUserDetails: (String)userId, (String)role */

    /**
     * 독자용
     * */
    // 회원가입한 경우 로그인
    public ResponseEntity<CustomResponse<ReaderSocialLoginResponse>> readerLoginWithIdToken(String idToken, OAuthProvider provider) {
        OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(idToken, provider);

        AuthUserDetails userDetails = readerLoginService.execute(oauthInfo);
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
        OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(idToken, provider);

        OAuthLoginWithTokenResponse onboardingToken = tokenGenerateHelper.generateOAuthLoginWithToken(oauthInfo);

        ReaderPreLoginResponse readerPreLoginResponse = new ReaderPreLoginResponse(
                onboardingToken.onboardingToken()
        );

        return ResponseEntity.ok()
                .body(CustomResponse.onSuccess(SuccessCode.OAUTH_PRE_LOGIN_SUCCESS,
                        new ReaderSocialLoginResponse(false, null, readerPreLoginResponse)));
    }

}
