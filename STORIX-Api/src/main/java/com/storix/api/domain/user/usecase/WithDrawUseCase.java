package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.Role;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.service.AuthService;
import com.storix.api.domain.user.helper.OAuthHelper;
import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class WithDrawUseCase {

    private final AuthService authService;
    private final UserAdaptor userAdaptor;

    private final OAuthHelper oauthHelper;
    private final CookieHelper cookieHelper;

    @Transactional
    public ResponseEntity<CustomResponse<Void>> execute(Long userId) {
        User user = userAdaptor.findUserById(userId);
        if (user.getRole() == Role.READER) {
            switch (user.getOauthInfo().getProvider()) {
                case KAKAO -> oauthHelper.unlinkKakaoUser(user.getOauthInfo().getOid());
                case NAVER -> oauthHelper.unlinkNaverUser(user.getOauthInfo().getOid());
            }
        }
        authService.withDrawUser(userId);
        return ResponseEntity.ok()
                .headers(cookieHelper.deleteCookie())
                .body(CustomResponse.onSuccess(SuccessCode.AUTH_WITHDRAW_SUCCESS));
    }
}
