package com.storix.domain.domains.user.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.onboarding.service.OnboardingWorksHelper;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.adaptor.InternalSignupPendingAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.*;
import com.storix.domain.domains.user.dto.CreateTesterUserCommand;
import com.storix.domain.domains.user.exception.tester.TesterNotApprovedException;
import com.storix.domain.domains.user.exception.tester.TesterSignupPendingNotFoundException;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class TesterAuthService {

    private final UserAdaptor userAdaptor;
    private final OnboardingWorksHelper onboardingWorksHelper;
    private final LibraryAdaptor libraryAdaptor;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;
    private final InternalSignupPendingAdaptor internalSignupPendingAdaptor;
    private final IssuedKeyGenerator issuedKeyGenerator;

    // 테스터 회원가입 요청 (Slack 승인 대기)
    public TesterSignupPending requestSignup(String nickName, Set<Genre> favoriteGenreList, Set<Long> favoriteWorksIdList) {
        if (favoriteWorksIdList != null && !favoriteWorksIdList.isEmpty()) {
            onboardingWorksHelper.checkReaderSignUpWithOnboardingWorksList(favoriteWorksIdList);
        }

        String pendingId = issuedKeyGenerator.generatePendingId();

        TesterSignupPending pending = TesterSignupPending.builder()
                .pendingId(pendingId)
                .nickName(nickName)
                .favoriteGenreList(favoriteGenreList)
                .favoriteWorksIdList(favoriteWorksIdList)
                .ttl(600L) // 10분
                .build();

        internalSignupPendingAdaptor.save(pending);
        return pending;
    }

    // Slack 승인 후 유저 생성
    @Transactional
    public AuthUserDetails approveTesterSignup(String pendingId) {
        TesterSignupPending pending = internalSignupPendingAdaptor.getTesterPending(pendingId);

        CreateTesterUserCommand cmd = CreateTesterUserCommand.builder()
                .oid(pendingId)
                .nickName(pending.getNickName())
                .favoriteGenreList(pending.getFavoriteGenreList())
                .build();

        AuthUserDetails authUserDetails = userAdaptor.saveTesterUser(cmd);

        if (pending.getFavoriteWorksIdList() != null && !pending.getFavoriteWorksIdList().isEmpty()) {
            favoriteWorksAdaptor.saveFavoriteWorks(authUserDetails.getUserId(), pending.getFavoriteWorksIdList());
        }
        libraryAdaptor.initLibrary(authUserDetails.getUserId());

        internalSignupPendingAdaptor.deleteTesterPending(pendingId);
        return authUserDetails;
    }

    // 테스터 로그인 (pendingId = oid)
    @Transactional
    public AuthUserDetails loginTester(String pendingId) {
        try {
            User user = userAdaptor.findReaderUserByOAuthInfo(
                    OAuthInfo.builder()
                            .provider(OAuthProvider.SLACK)
                            .oid(pendingId)
                            .build());
            user.login();
            return new AuthUserDetails(user.getId(), user.getRole());
        } catch (Exception e) {
            if (internalSignupPendingAdaptor.existsTesterPending(pendingId)) {
                throw TesterNotApprovedException.EXCEPTION;
            }
            throw TesterSignupPendingNotFoundException.EXCEPTION;
        }
    }
}
