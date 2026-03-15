package com.storix.storix_api.domains.user.application.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.user.adaptor.TokenAdaptor;
import com.storix.storix_api.domains.user.application.usecase.helper.CookieHelper;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class LogoutUseCase {

    private final TokenAdaptor tokenAdaptor;

    private final CookieHelper cookieHelper;

    @Transactional
    public ResponseEntity<CustomResponse<Void>> execute(Long userId) {
        tokenAdaptor.deleteRefreshTokenByUserId(userId);
        return ResponseEntity.ok()
                .headers(cookieHelper.deleteCookie())
                .body(CustomResponse.onSuccess(SuccessCode.AUTH_LOGOUT_SUCCESS));
    }

}
