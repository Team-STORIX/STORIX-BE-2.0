package com.storix.api.domain.user.helper;

import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.dto.KakaoUserResponse;
import com.storix.domain.domains.user.dto.NaverUserResponse;
import com.storix.domain.domains.user.dto.ValidAuthDTO;
import com.storix.domain.domains.user.dto.XUserResponse;
import com.storix.domain.domains.user.exception.me.UnknownUserException;
import com.storix.domain.domains.user.exception.oauth.FeignClientServerErrorException;
import com.storix.domain.domains.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthSignupValidator {

    private final OAuthHelper oauthHelper;
    private final AuthService authService;

    // Kakao 공통 검증 로직
    // - isNative=true 이면 idToken의 aud(audience) 검증을 Native App Key 로 수행
    //   (Kakao는 Web=REST API Key / Native=Native App Key 로 idToken.aud 가 다름)
    public ValidAuthDTO validateKakao(String accessToken, String idToken, boolean isNative) {
        // accessToken으로 사용자 정보 조회
        KakaoUserResponse kakaoUser = oauthHelper.getKakaoInformation(accessToken);
        // idToken으로 OIDC 검증
        OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(idToken, null, OAuthProvider.KAKAO, isNative);

        // token 간 정보 일치 확인 후 회원가입 여부 반환
        if (!oauthInfo.getOid().equals(kakaoUser.id())) throw UnknownUserException.EXCEPTION;
        return authService.validKakaoSignup(kakaoUser.id(), idToken);
    }

    // Naver 공통 검증 로직
    public ValidAuthDTO validateNaver(String accessToken, String refreshToken) {
        // accessToken으로 사용자 정보 조회
        NaverUserResponse naverUser = oauthHelper.getNaverInformation(accessToken);

        // token 간 정보 일치 확인 후 회원가입 여부 반환
        if (naverUser.id() == null) throw FeignClientServerErrorException.EXCEPTION;
        return authService.validNaverSignup(naverUser.id(), refreshToken);
    }

    // Apple 공통 검증 로직
    // - Apple은 Web/Native 모두 동일한 clientId(서비스 ID) 를 aud 로 사용하므로 isNative 구분 불필요 → false.
    // - refresh_token은 탈퇴 시 연동 해제(revoke)용으로 보관
    public ValidAuthDTO validateApple(String idToken, String refreshToken) {
        OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(idToken, null, OAuthProvider.APPLE, false);
        return authService.validAppleSignup(oauthInfo.getOid(), idToken, refreshToken);
    }

    // X 공통 검증 로직
    // accessToken으로 oid 조회, refresh_token은 탈퇴 revoke용 best-effort 보관
    public ValidAuthDTO validateX(String accessToken, String refreshToken) {
        XUserResponse xUser = oauthHelper.getXInformation(accessToken);

        if (xUser == null || xUser.id() == null) throw FeignClientServerErrorException.EXCEPTION;
        return authService.validXSignup(xUser.id(), refreshToken);
    }
}
