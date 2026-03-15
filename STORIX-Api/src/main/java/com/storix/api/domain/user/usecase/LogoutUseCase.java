package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.user.adaptor.TokenAdaptor;
import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
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
