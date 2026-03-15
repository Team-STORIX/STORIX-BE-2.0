package com.storix.storix_api.domains.user.application.service;

import com.storix.storix_api.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.storix_api.domains.library.adaptor.LibraryAdaptor;
import com.storix.storix_api.domains.onboarding.helper.OnboardingWorksHelper;
import com.storix.storix_api.domains.user.controller.dto.ArtistSignupRequest;
import com.storix.storix_api.domains.user.controller.dto.OAuthAuthorizationRequest;
import com.storix.storix_api.domains.user.controller.dto.ReaderSignupRequest;
import com.storix.storix_api.domains.user.controller.dto.ValidAuthDTO;
import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.domains.user.adaptor.TokenAdaptor;
import com.storix.storix_api.domains.user.application.usecase.helper.OAuthHelper;
import com.storix.storix_api.domains.user.adaptor.UserAdaptor;
import com.storix.storix_api.domains.user.domain.OAuthInfo;
import com.storix.storix_api.domains.user.domain.OAuthProvider;
import com.storix.storix_api.domains.user.domain.Role;
import com.storix.storix_api.domains.user.domain.User;
import com.storix.storix_api.domains.user.dto.*;
import com.storix.storix_api.global.apiPayload.exception.user.*;
import com.storix.storix_api.global.apiPayload.exception.web.FeignClientServerErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAdaptor userAdaptor;
    private final OnboardingWorksHelper onboardingWorksHelper;
    private final LibraryAdaptor libraryAdaptor;
    private final TokenAdaptor tokenAdaptor;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;
    private final OAuthHelper oauthHelper;
    private final PasswordEncoder passwordEncoder;

    // 독자 회원 가입 가능 여부 (토큰 검증, 계정 정보 유무)
    // - 카카오
    @Transactional
    public ValidAuthDTO validKakaoSignup(OAuthAuthorizationRequest req) {

        KakaoTokenResponse kakaoToken = oauthHelper.getKakaoOAuthToken(req.authCode(), req.redirectUri());
        KakaoUserResponse kakaoUser = oauthHelper.getKakaoInformation(kakaoToken.accessToken());

        OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(kakaoToken.idToken(), OAuthProvider.KAKAO);
        if (!oauthInfo.getOid().equals(kakaoUser.id())) { throw UnknownUserException.EXCEPTION; }

        boolean isRegistered = userAdaptor.isUserPresentWithProviderAndOid(OAuthProvider.KAKAO, kakaoUser.id());

        return new ValidAuthDTO(isRegistered, kakaoToken.idToken());
    }

    // - 네이버
    @Transactional
    public ValidAuthDTO validNaverSignup(OAuthAuthorizationRequest req) {

        NaverTokenResponse naverToken = oauthHelper.getNaverOAuthToken(req.authCode(), req.state());
        NaverUserResponse naverUser = oauthHelper.getNaverInformation(naverToken.accessToken());

        // 네이버 OIDC token 요청 시 Internal Server Error 반환 중 -> 지원 종료 관련 공지는 없으나, 더이상 지원하지 않는다 판단
        // 기존 로직에서 idToken 값에 oid 값 반환
//        OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(naverToken.idToken(), OAuthProvider.NAVER);
//        if (!oauthInfo.getOid().equals(naverUser.id())) { throw UnknownUserException.EXCEPTION; }

        if (naverUser.id() == null) throw FeignClientServerErrorException.EXCEPTION;

        boolean isRegistered = userAdaptor.isUserPresentWithProviderAndOid(OAuthProvider.NAVER, naverUser.id());

//        return new ValidAuthDTO(isRegistered, naverToken.idToken());
        return new ValidAuthDTO(isRegistered, naverUser.id());
    }

    // 독자 회원 가입 (소셜 로그인)
    @Transactional
    public AuthUserDetails signUpReaderUser(ReaderSignupRequest req, String jti) {

        OnboardingPrincipal principal = tokenAdaptor.findOnboardingPrincipalByJti(jti);
        OAuthProvider provider = principal.provider(); String oid = principal.oid();

        boolean isUserPresent = userAdaptor.isUserPresentWithProviderAndOid(provider, oid);
        if (isUserPresent) throw DuplicateUserException.EXCEPTION;

        onboardingWorksHelper.checkReaderSignUpWithOnboardingWorksList(req.favoriteWorksIdList());

        userAdaptor.checkNicknameDuplicate(req.nickName());

        CreateReaderUserCommand m = new CreateReaderUserCommand(
                req.marketingAgree(),
                provider,
                oid,
                req.nickName(),
                req.gender(),
                req.favoriteGenreList()
        );

        AuthUserDetails authUserDetails = userAdaptor.saveReaderUser(m);
        tokenAdaptor.deleteOnboardingTokenByJti(jti);

        favoriteWorksAdaptor.saveFavoriteWorks(authUserDetails.getUserId(), req.favoriteWorksIdList());
        libraryAdaptor.initLibrary(authUserDetails.getUserId());

        return authUserDetails;
    }

    // 독자 닉네임 중복 체크
    public void validNickname(String nickName) {
        userAdaptor.checkNicknameDuplicateWithArtists(nickName);
        userAdaptor.checkNicknameDuplicate(nickName);
    }

    // 작가 회원 가입 (일반 로그인)
    @Transactional
    public Long signUpArtistUser(ArtistSignupRequest req) {
        userAdaptor.checkLoginIdDuplicate(req.loginId());

        CreateArtistUserCommand m = new CreateArtistUserCommand(
                req.nickName(),
                req.loginId(),
                passwordEncoder.encode(req.password())
        );

        userAdaptor.saveArtistUser(m);
        Long artistUserId = userAdaptor.findArtistUserIdByLoginId(req.loginId());
        // TODO: 이때 WorksAndArtistMatcher 만들어두고 artistUserId 바로 넘기면 될듯요 (회원가입과 동시에 Works에 작가 회원id 정보 넣어주기)

        return artistUserId;
    }

    // 유저 회원 탈퇴
    @Transactional
    public void withDrawUser(Long userId) {
        User user = userAdaptor.findUserById(userId);
        // 소셜 서비스 unlink
        if (user.getRole() == Role.READER) {
            switch (user.getOauthInfo().getProvider()) {
                case KAKAO -> oauthHelper.unlinkKakaoUser(user.getOauthInfo().getOid());
                case NAVER -> oauthHelper.unlinkNaverUser(user.getOauthInfo().getOid());
            }
        }
        user.withdraw();
        tokenAdaptor.deleteRefreshTokenByUserId(userId);

        favoriteWorksAdaptor.deleteFavoriteWorks(userId);
        libraryAdaptor.deleteLibrary(userId);
    }
}
