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

    public ResponseEntity<CustomResponse<AuthorizationResponse>> getTokenRefresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) throw RefreshTokenNotExistException.EXCEPTION;

        LoginWithTokenResponse tokenResponse = tokenGenerateHelper.reissueTokens(refreshToken);
        AuthorizationResponse result = new AuthorizationResponse(tokenResponse.accessToken());

        return ResponseEntity.ok()
                .headers(cookieHelper.getTokenCookie(tokenResponse.refreshToken()))
                .body(CustomResponse.onSuccess(SuccessCode.AUTH_REISSUE_ACCESSTOKEN_SUCCESS, result));
    }
}