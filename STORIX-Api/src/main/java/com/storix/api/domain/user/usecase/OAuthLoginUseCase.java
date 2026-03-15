package com.storix.api.domain.user.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.user.dto.OAuthAuthorizationRequest;
import com.storix.api.domain.user.controller.dto.ReaderSocialLoginResponse;
import com.storix.domain.domains.user.dto.ValidAuthDTO;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.common.payload.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@UseCase
@RequiredArgsConstructor
public class OAuthLoginUseCase {

    private final AuthUseCase authUseCase;
    private final LoginUseCase loginUseCase;

    public ResponseEntity<CustomResponse<ReaderSocialLoginResponse>> readerOAuthLogin(OAuthAuthorizationRequest req, OAuthProvider provider) {
        ValidAuthDTO valid = authUseCase.checkAvailableRegister(req, provider);

        /**
         * (1) isRegistered = true (계정 정보 있음) -> 로그인 시키기
         *     return 1)isRegistered 2)AccessToken 3)RefreshToken
         * */
        if (valid.isRegistered()) {
            return loginUseCase.readerLoginWithIdToken(valid.idToken(), provider);
        }
        /**
         * (2) isRegistered = false (계정 정보 없음) -> 회원가입에 필요한 유저 정보를 담은 온보딩 토큰 반환 (OAuthInfo)
         *     return 1)isRegistered 2)OnboardingToken
         * */
        else {
            return loginUseCase.readerPreLoginWithIdToken(valid.idToken(), provider);
        }
    }

}