package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.api.domain.user.controller.dto.AuthorizationResponse;
import com.storix.api.domain.user.helper.TokenGenerateHelper;
import com.storix.api.domain.user.controller.dto.LoginWithTokenResponse;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import com.storix.domain.domains.user.exception.token.RefreshTokenNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@UseCase
@RequiredArgsConstructor
public class AuthorizationUseCase {

    private final TokenGenerateHelper tokenGenerateHelper;

    private final CookieHelper cookieHelper;

    // 토큰 재발급
    public ResponseEntity<CustomResponse<AuthorizationResponse>> getTokenRefresh(
            String cookieRefreshToken, String bodyRefreshToken
    ) {
        boolean useBodyToken = cookieRefreshToken == null || cookieRefreshToken.isBlank();
        String refreshToken = useBodyToken ? bodyRefreshToken : cookieRefreshToken;

        if (refreshToken == null || refreshToken.isBlank()) throw RefreshTokenNotExistException.EXCEPTION;

        LoginWithTokenResponse tokenResponse = tokenGenerateHelper.reissueTokens(refreshToken);

        // Native: body로 access/refresh 둘 다 반환
        if (useBodyToken) {
            return ResponseEntity.ok()
                    .body(CustomResponse.onSuccess(SuccessCode.AUTH_REISSUE_ACCESSTOKEN_SUCCESS,
                            AuthorizationResponse.nativeRefresh(tokenResponse.accessToken(), tokenResponse.refreshToken())));
        }

        // Web: accessToken만 body, refreshToken은 Set-Cookie 로 재발급
        return ResponseEntity.ok()
                .headers(cookieHelper.getTokenCookie(tokenResponse.refreshToken()))
                .body(CustomResponse.onSuccess(SuccessCode.AUTH_REISSUE_ACCESSTOKEN_SUCCESS,
                        AuthorizationResponse.webRefresh(tokenResponse.accessToken())));
    }
}
