package com.storix.domain.domains.user.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.onboarding.service.OnboardingWorksHelper;
import com.storix.domain.domains.user.dto.CreateReaderUserCommand;
import com.storix.domain.domains.user.dto.OnboardingPrincipal;
import com.storix.domain.domains.user.dto.ReaderSignupRequest;
import com.storix.domain.domains.user.dto.ValidAuthDTO;
import com.storix.domain.domains.user.exception.me.DuplicateUserException;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.adaptor.TokenAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
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

    // 독자 회원 가입 (소셜 로그인)
    @Transactional
    public AuthUserDetails signUpReaderUser(ReaderSignupRequest cmd, String jti) {

        OnboardingPrincipal principal = tokenAdaptor.findOnboardingPrincipalByJti(jti);
        OAuthProvider provider = principal.provider(); String oid = principal.oid();

        boolean isUserPresent = userAdaptor.isUserPresentWithProviderAndOid(provider, oid);
        if (isUserPresent) throw DuplicateUserException.EXCEPTION;

        if (cmd.favoriteWorksIdList() != null && !cmd.favoriteWorksIdList().isEmpty()) {
            onboardingWorksHelper.checkReaderSignUpWithOnboardingWorksList(cmd.favoriteWorksIdList());
        }

        userAdaptor.checkNicknameDuplicate(cmd.nickName());

        CreateReaderUserCommand m = new CreateReaderUserCommand(
                cmd.marketingAgree(),
                provider,
                oid,
                cmd.nickName(),
                cmd.favoriteGenreList()
        );

        AuthUserDetails authUserDetails = userAdaptor.saveReaderUser(m);
        tokenAdaptor.deleteOnboardingTokenByJti(jti);

        if (cmd.favoriteWorksIdList() != null && !cmd.favoriteWorksIdList().isEmpty()) {
            favoriteWorksAdaptor.saveFavoriteWorks(authUserDetails.getUserId(), cmd.favoriteWorksIdList());
        }
        libraryAdaptor.initLibrary(authUserDetails.getUserId());

        return authUserDetails;
    }

    // 독자 닉네임 중복 체크
    public void validNickname(String nickName) {
        userAdaptor.checkNicknameDuplicate(nickName);
    }

    // 유저 회원 탈퇴
    @Transactional
    public void withDrawUser(Long userId) {
        User user = userAdaptor.findUserById(userId);
        user.withdraw();
        tokenAdaptor.deleteRefreshTokenByUserId(userId);
        favoriteWorksAdaptor.deleteFavoriteWorks(userId);
        libraryAdaptor.deleteLibrary(userId);
    }
}
