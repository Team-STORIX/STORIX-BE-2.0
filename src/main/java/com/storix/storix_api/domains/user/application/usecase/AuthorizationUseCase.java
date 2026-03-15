package com.storix.storix_api.domains.user.application.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.user.application.usecase.helper.CookieHelper;
import com.storix.storix_api.domains.user.controller.dto.AuthorizationResponse;
import com.storix.storix_api.domains.user.application.usecase.helper.TokenGenerateHelper;
import com.storix.storix_api.domains.user.controller.dto.LoginWithTokenResponse;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import com.storix.storix_api.global.apiPayload.exception.cookie.RefreshTokenNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@UseCase
@RequiredArgsConstructor
public class AuthorizationUseCase {

    private final TokenGenerateHelper tokenGenerateHelper;

    private final CookieHelper cookieHelper;

    public ResponseEntity<CustomResponse<AuthorizationResponse>> getTokenRefresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) throw RefreshTokenNotExistException.EXCEPTION;

        LoginWithTokenResponse tokenResponse = tokenGenerateHelper.reissueTokens(refreshToken);
        AuthorizationResponse result = new AuthorizationResponse(tokenResponse.accessToken());

        return ResponseEntity.ok()
                .headers(cookieHelper.getTokenCookie(tokenResponse.refreshToken()))
                .body(CustomResponse.onSuccess(SuccessCode.AUTH_REISSUE_ACCESSTOKEN_SUCCESS, result));
    }
}