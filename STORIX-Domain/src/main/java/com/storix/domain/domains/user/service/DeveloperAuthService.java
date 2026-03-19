package com.storix.domain.domains.user.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.onboarding.service.OnboardingWorksHelper;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.*;
import com.storix.domain.domains.user.dto.CreateDeveloperUserCommand;
import com.storix.domain.domains.user.exception.developer.DeveloperNotApprovedException;
import com.storix.domain.domains.user.exception.developer.DeveloperSignupPendingNotFoundException;
import com.storix.domain.domains.user.repository.DeveloperSignupPendingRepository;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeveloperAuthService {

    private final UserAdaptor userAdaptor;
    private final OnboardingWorksHelper onboardingWorksHelper;
    private final LibraryAdaptor libraryAdaptor;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;
    private final DeveloperSignupPendingRepository pendingRepository;

    // 개발자 회원가입 요청 (Slack 승인 대기)
    public DeveloperSignupPending requestSignup(String nickName, Set<Genre> favoriteGenreList, Set<Long> favoriteWorksIdList) {
        if (favoriteWorksIdList != null && !favoriteWorksIdList.isEmpty()) {
            onboardingWorksHelper.checkReaderSignUpWithOnboardingWorksList(favoriteWorksIdList);
        }

        String pendingId = generatePendingId();

        DeveloperSignupPending pending = DeveloperSignupPending.builder()
                .pendingId(pendingId)
                .nickName(nickName)
                .favoriteGenreList(favoriteGenreList)
                .favoriteWorksIdList(favoriteWorksIdList)
                .ttl(600L) // 10분
                .build();

        pendingRepository.save(pending);
        return pending;
    }

    // Slack 승인 후 유저 생성
    @Transactional
    public AuthUserDetails approveDeveloperSignup(String pendingId) {
        DeveloperSignupPending pending = pendingRepository.findById(pendingId)
                .orElseThrow(() -> DeveloperSignupPendingNotFoundException.EXCEPTION);

        CreateDeveloperUserCommand cmd = new CreateDeveloperUserCommand(
                pendingId,
                pending.getNickName(),
                pending.getFavoriteGenreList()
        );

        AuthUserDetails authUserDetails = userAdaptor.saveDeveloperUser(cmd);

        if (pending.getFavoriteWorksIdList() != null && !pending.getFavoriteWorksIdList().isEmpty()) {
            favoriteWorksAdaptor.saveFavoriteWorks(authUserDetails.getUserId(), pending.getFavoriteWorksIdList());
        }
        libraryAdaptor.initLibrary(authUserDetails.getUserId());

        pendingRepository.deleteById(pendingId);
        return authUserDetails;
    }

    private String generatePendingId() {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    // 개발자 로그인 (pendingId = oid)
    @Transactional
    public AuthUserDetails loginDeveloper(String pendingId) {
        try {
            User user = userAdaptor.findReaderUserByOAuthInfo(
                    new OAuthInfo(OAuthProvider.SLACK, pendingId));
            user.login();
            return new AuthUserDetails(user.getId(), user.getRole());
        } catch (Exception e) {
            if (pendingRepository.existsById(pendingId)) {
                throw DeveloperNotApprovedException.EXCEPTION;
            }
            throw DeveloperSignupPendingNotFoundException.EXCEPTION;
        }
    }
}
