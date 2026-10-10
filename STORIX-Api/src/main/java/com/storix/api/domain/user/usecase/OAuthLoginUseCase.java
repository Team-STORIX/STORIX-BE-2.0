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
        // 가입한 유저는 로그인 토큰, 아니면 온보딩 토큰
        return valid.isRegistered()
                ? loginUseCase.readerLoginWithIdToken(valid.idToken(), valid.oid(), provider, false, valid.oauthRefreshToken())
                : loginUseCase.readerPreLoginWithIdToken(valid.idToken(), valid.oid(), provider, false, valid.oauthRefreshToken());
    }

    // Native: Kakao/Naver SDK에서 받은 accessToken(+idToken)을 그대로 검증
    public ResponseEntity<CustomResponse<ReaderSocialLoginResponse>> readerOAuthNativeLogin(OAuthAuthorizationRequest req, OAuthProvider provider) {
        ValidAuthDTO valid = authUseCase.checkAvailableRegisterNative(req, provider);
        // 가입한 유저는 로그인 토큰, 아니면 온보딩 토큰
        return valid.isRegistered()
                ? loginUseCase.readerLoginWithIdToken(valid.idToken(), valid.oid(), provider, true, valid.oauthRefreshToken())
                : loginUseCase.readerPreLoginWithIdToken(valid.idToken(), valid.oid(), provider, true, valid.oauthRefreshToken());
    }
}
