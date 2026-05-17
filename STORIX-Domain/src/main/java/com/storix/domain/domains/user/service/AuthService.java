package com.storix.domain.domains.user.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.genrescore.event.GenreScoreEventType;
import com.storix.domain.domains.genrescore.publisher.GenreScorePublisher;
import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.onboarding.service.OnboardingWorksHelper;
import com.storix.domain.domains.pushdevice.adaptor.PushDeviceAdaptor;
import com.storix.domain.domains.user.dto.CreateReaderUserCommand;
import com.storix.domain.domains.user.dto.OnboardingPrincipal;
import com.storix.domain.domains.user.dto.ReaderSignUpData;
import com.storix.domain.domains.user.dto.ValidAuthDTO;
import com.storix.domain.domains.user.exception.me.DuplicateUserException;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.adaptor.TokenAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAdaptor userAdaptor;
    private final TokenAdaptor tokenAdaptor;
    private final LibraryAdaptor libraryAdaptor;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;

    private final PushDeviceAdaptor pushDeviceAdaptor;

    private final OnboardingWorksHelper onboardingWorksHelper; // -> usecase 리팩토링 필요
    private final GenreScorePublisher genreScorePublisher;

    // 독자 회원 가입 가능 여부 (토큰 검증, 계정 정보 유무)
    // - 카카오
    @Transactional(readOnly = true)
    public ValidAuthDTO validKakaoSignup(String kakaoUserId, String idToken) {
        boolean isRegistered = userAdaptor.isUserPresentWithProviderAndOid(OAuthProvider.KAKAO, kakaoUserId);
        return new ValidAuthDTO(isRegistered, idToken);
    }

    // - 네이버
    @Transactional(readOnly = true)
    public ValidAuthDTO validNaverSignup(String naverUserId) {
        boolean isRegistered = userAdaptor.isUserPresentWithProviderAndOid(OAuthProvider.NAVER, naverUserId);
        return new ValidAuthDTO(isRegistered, naverUserId);
    }

    // - 애플
    @Transactional(readOnly = true)
    public ValidAuthDTO validAppleSignup(String appleUserId, String idToken) {
        boolean isRegistered = userAdaptor.isUserPresentWithProviderAndOid(OAuthProvider.APPLE, appleUserId);
        return new ValidAuthDTO(isRegistered, idToken);
    }

    // 독자 회원 가입 (소셜 로그인)
    @Transactional
    public AuthUserDetails signUpReaderUser(ReaderSignUpData cmd, String jti) {

        OnboardingPrincipal principal = tokenAdaptor.findOnboardingPrincipalByJti(jti);
        OAuthProvider provider = principal.provider(); String oid = principal.oid();

        boolean isUserPresent = userAdaptor.isUserPresentWithProviderAndOid(provider, oid);
        if (isUserPresent) throw DuplicateUserException.EXCEPTION;

        if (cmd.favoriteWorksIdList() != null && !cmd.favoriteWorksIdList().isEmpty()) {
            onboardingWorksHelper.checkReaderSignUpWithOnboardingWorksList(cmd.favoriteWorksIdList());
        }

        userAdaptor.checkNicknameDuplicate(cmd.nickName());

        CreateReaderUserCommand m = new CreateReaderUserCommand(
                cmd.termsAgree(),
                provider,
                oid,
                cmd.nickName(),
                cmd.favoriteGenreList(),
                cmd.profileDescription()
        );

        AuthUserDetails authUserDetails = userAdaptor.saveReaderUser(m);
        tokenAdaptor.deleteOnboardingTokenByJti(jti);

        if (cmd.favoriteWorksIdList() != null && !cmd.favoriteWorksIdList().isEmpty()) {
            favoriteWorksAdaptor.saveFavoriteWorks(authUserDetails.getUserId(), cmd.favoriteWorksIdList());
            genreScorePublisher.publishBatch(
                    authUserDetails.getUserId(),
                    cmd.favoriteWorksIdList(),
                    GenreScoreEventType.ONBOARDING_SELECT);
        }
        libraryAdaptor.initLibrary(authUserDetails.getUserId());

        return authUserDetails;
    }

    // 독자 닉네임 중복 체크
    public void validNickname(String nickName) {
        userAdaptor.checkNicknameDuplicate(nickName);
    }

    // 유저 OAuth 정보 조회
    @Transactional(readOnly = true)
    public OAuthInfo findOAuthInfoByUserId(Long userId) {
        return userAdaptor.findUserById(userId).getOauthInfo();
    }

    // 유저 로그아웃
    @Transactional
    public void logout(Long userId, String installationId) {
        // 1. refreshToken 삭제 (Redis)
        tokenAdaptor.deleteRefreshTokenByUserId(userId);

        // 2. [Native] 해당 디바이스 FCM 토큰 비활성화
        if (StringUtils.hasText(installationId)) {
            pushDeviceAdaptor.deactivateByUserIdAndInstallationId(userId, installationId);
        }
    }

    // 유저 회원 탈퇴
    @Transactional
    public void withDrawUser(Long userId) {
        // 1. 유저 soft-delete
        User user = userAdaptor.findUserById(userId);
        user.withdraw();

        // 2. 유저 관련 정보 (refresh 토큰, 관심 작품, 서재) 삭제
        tokenAdaptor.deleteRefreshTokenByUserId(userId);
        favoriteWorksAdaptor.deleteFavoriteWorks(userId);
        libraryAdaptor.deleteLibrary(userId);

        // 3. 푸시 알림 디바아스 일괄 비활성화
        pushDeviceAdaptor.deactivateAllByUserId(userId);
    }
}
