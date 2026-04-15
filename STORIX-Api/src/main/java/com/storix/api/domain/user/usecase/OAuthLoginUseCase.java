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

    // Web: authCode로 accessToken(+idToken) 요청 및 검증
    public ResponseEntity<CustomResponse<ReaderSocialLoginResponse>> readerOAuthLogin(OAuthAuthorizationRequest req, OAuthProvider provider) {
        ValidAuthDTO valid = authUseCase.checkAvailableRegister(req, provider);
        return dispatchLoginByRegistration(valid, provider);
    }

    // Native: Kakao/Naver SDK에서 받은 accessToken(+idToken)을 그대로 검증
    public ResponseEntity<CustomResponse<ReaderSocialLoginResponse>> readerOAuthNativeLogin(OAuthAuthorizationRequest req, OAuthProvider provider) {
        ValidAuthDTO valid = authUseCase.checkAvailableRegisterNative(req, provider);
        return dispatchLoginByRegistration(valid, provider);
    }

    /**
     * 회원 등록 여부에 따라 로그인 응답을 분기
     *
     * (1) isRegistered = true  -> 액세스 토큰 + 리프레쉬 토큰 쿠키 반환
     * (2) isRegistered = false -> 온보딩 토큰 반환 (회원가입 필요)
     */
    private ResponseEntity<CustomResponse<ReaderSocialLoginResponse>> dispatchLoginByRegistration(
            ValidAuthDTO valid, OAuthProvider provider
    ) {
        if (valid.isRegistered()) {
            return loginUseCase.readerLoginWithIdToken(valid.idToken(), provider);
        }
        return loginUseCase.readerPreLoginWithIdToken(valid.idToken(), provider);
    }

}
