package com.storix.domain.domains.user.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.genrescore.event.GenreScoreEventType;
import com.storix.domain.domains.genrescore.publisher.GenreScorePublisher;
import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.notification.adaptor.NotificationSettingAdaptor;
import com.storix.domain.domains.onboarding.service.OnboardingWorksHelper;
import com.storix.domain.domains.pushdevice.adaptor.PushDeviceAdaptor;
import com.storix.domain.domains.user.adaptor.TermsAdaptor;
import com.storix.domain.domains.user.domain.Terms;
import com.storix.domain.domains.user.domain.TermsType;
import com.storix.domain.domains.user.domain.UserTermHistory;
import com.storix.domain.domains.user.dto.CreateReaderUserCommand;
import com.storix.domain.domains.user.dto.OnboardingPrincipal;
import com.storix.domain.domains.user.dto.ReaderSignUpData;
import com.storix.domain.domains.user.dto.ValidAuthDTO;
import com.storix.domain.domains.user.exception.me.DuplicateUserException;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.adaptor.TokenAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserHistoryAdaptor;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.domain.UserHistory;
import com.storix.domain.domains.user.domain.UserHistoryType;
import com.storix.domain.domains.user.domain.WithdrawReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAdaptor userAdaptor;
    private final TokenAdaptor tokenAdaptor;
    private final LibraryAdaptor libraryAdaptor;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;

    private final PushDeviceAdaptor pushDeviceAdaptor;
    private final NotificationSettingAdaptor notificationSettingAdaptor;
    private final UserHistoryAdaptor userHistoryAdaptor;
    private final TermsAdaptor termsAdaptor;

    private final OnboardingWorksHelper onboardingWorksHelper; // -> usecase 리팩토링 필요
    private final GenreScorePublisher genreScorePublisher;

    // 독자 회원 가입 가능 여부 (토큰 검증, 계정 정보 유무)
    // - 카카오
    @Transactional(readOnly = true)
    public ValidAuthDTO validKakaoSignup(String kakaoUserId, String idToken) {
        boolean isRegistered = userAdaptor.isUserPresentWithProviderAndOid(OAuthProvider.KAKAO, kakaoUserId);
        return ValidAuthDTO.ofIdToken(isRegistered, idToken);
    }

    // - 네이버
    @Transactional(readOnly = true)
    public ValidAuthDTO validNaverSignup(String naverUserId, String oauthRefreshToken) {
        boolean isRegistered = userAdaptor.isUserPresentWithProviderAndOid(OAuthProvider.NAVER, naverUserId);
        return ValidAuthDTO.ofOid(isRegistered, naverUserId, oauthRefreshToken);
    }

    // - 애플
    @Transactional(readOnly = true)
    public ValidAuthDTO validAppleSignup(String appleUserId, String idToken, String oauthRefreshToken) {
        boolean isRegistered = userAdaptor.isUserPresentWithProviderAndOid(OAuthProvider.APPLE, appleUserId);
        return ValidAuthDTO.ofIdToken(isRegistered, idToken, oauthRefreshToken);
    }

    // - X
    @Transactional(readOnly = true)
    public ValidAuthDTO validXSignup(String xUserId, String oauthRefreshToken) {
        boolean isRegistered = userAdaptor.isUserPresentWithProviderAndOid(OAuthProvider.X, xUserId);
        return ValidAuthDTO.ofOid(isRegistered, xUserId, oauthRefreshToken);
    }

    // 독자 회원 가입 (소셜 로그인)
    @Transactional
    public AuthUserDetails signUpReaderUser(ReaderSignUpData cmd, String jti) {

        // 1. 온보딩 토큰 정보 조회
        OnboardingPrincipal principal = tokenAdaptor.findOnboardingPrincipalByJti(jti);
        OAuthProvider provider = principal.provider(); String oid = principal.oid();
        String oauthRefreshToken = principal.oauthRefreshToken();

        // 2. 회원 가입 관련 검증
        boolean isUserPresent = userAdaptor.isUserPresentWithProviderAndOid(provider, oid);
        if (isUserPresent) throw DuplicateUserException.EXCEPTION;

        if (cmd.favoriteWorksIdList() != null && !cmd.favoriteWorksIdList().isEmpty()) {
            onboardingWorksHelper.checkReaderSignUpWithOnboardingWorksList(cmd.favoriteWorksIdList());
        }

        userAdaptor.checkNicknameDuplicate(cmd.nickName());

        // 3. 회원 가입 정보 DB 저장
        CreateReaderUserCommand m = new CreateReaderUserCommand(
                cmd.ageOver14(),
                provider,
                oid,
                oauthRefreshToken,
                cmd.nickName(),
                cmd.favoriteGenreList(),
                cmd.profileDescription()
        );
        AuthUserDetails authUserDetails = userAdaptor.saveReaderUser(m);

        tokenAdaptor.deleteOnboardingTokenByJti(jti);

        saveSignupTermsAgreements(authUserDetails.getUserId(), cmd);

        if (cmd.favoriteWorksIdList() != null && !cmd.favoriteWorksIdList().isEmpty()) {
            favoriteWorksAdaptor.saveFavoriteWorks(authUserDetails.getUserId(), cmd.favoriteWorksIdList());
            genreScorePublisher.publishBatch(
                    authUserDetails.getUserId(),
                    cmd.favoriteWorksIdList(),
                    GenreScoreEventType.ONBOARDING_SELECT);
        }

        libraryAdaptor.initLibrary(authUserDetails.getUserId());

        // 알림 설정 기본값 생성 (마케팅만 OFF, 나머지 ON)
        notificationSettingAdaptor.save(authUserDetails.getUserId());

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
    public void withDrawUser(Long userId, Set<WithdrawReason> reasons, String detail) {
        // 1. 유저 soft-delete
        User user = userAdaptor.findUserById(userId);
        user.withdraw();

        // 2. 유저 관련 정보 (refresh 토큰, 관심 작품, 서재) 삭제
        tokenAdaptor.deleteRefreshTokenForWithdrawByUserId(userId);
        favoriteWorksAdaptor.deleteFavoriteWorks(userId);
        libraryAdaptor.deleteLibrary(userId);

        // 3. 푸시 알림 디바이스 일괄 비활성화
        pushDeviceAdaptor.deactivateAllByUserId(userId);

        // 4. 알림 설정 삭제 (재가입 시 새 row 생성됨)
        notificationSettingAdaptor.deleteByUserId(userId);

        // 5. 탈퇴 사유 로그 저장
        saveWithdrawHistory(userId, reasons, detail);
    }


    // 회원 가입 시 필수 약관(서비스 이용약관/개인정보 수집·이용) 동의 이력 저장
    private void saveSignupTermsAgreements(Long userId, ReaderSignUpData cmd) {
        if (Boolean.TRUE.equals(cmd.serviceTermsAgree())) {
            recordTermAgreement(userId, TermsType.SERVICE);
        }
        if (Boolean.TRUE.equals(cmd.privacyPolicyAgree())) {
            recordTermAgreement(userId, TermsType.PRIVACY);
        }
    }

    // 해당 종류의 현재 약관에 대한 동의 이력 저장
    private void recordTermAgreement(Long userId, TermsType termsType) {
        Terms terms = termsAdaptor.findCurrentByType(termsType);
        userHistoryAdaptor.saveUserTermHistory(UserTermHistory.builder()
                .userId(userId)
                .terms(terms)
                .isAgreed(true)
                .agreedAt(LocalDateTime.now())
                .build());
    }

    // 회원탈퇴 사유 저장
    private void saveWithdrawHistory(Long userId, Set<WithdrawReason> reasons, String detail) {
        String otherDetail = (detail == null) ? null : detail.trim();
        LocalDateTime processedAt = LocalDateTime.now();

        for (WithdrawReason reason : reasons) {
            String detailValue = (reason == WithdrawReason.OTHER) ? otherDetail : null;
            userHistoryAdaptor.save(UserHistory.builder()
                    .userId(userId)
                    .historyType(UserHistoryType.WITHDRAW)
                    .processor(STORIXStatic.UserHistory.PROCESSOR_TEAM_STORIX)
                    .processedAt(processedAt)
                    .reason(reason)
                    .detail(detailValue)
                    .build());
        }
    }
}
