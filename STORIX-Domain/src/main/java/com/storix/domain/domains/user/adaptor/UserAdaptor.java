package com.storix.domain.domains.user.adaptor;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.dto.*;
import com.storix.domain.domains.user.exception.me.*;
import com.storix.domain.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAdaptor {

    @Value("${AWS_S3_BASE_URL}") private String baseUrl;

    private final UserRepository userRepository;

    public StandardProfileInfo findStandardProfileInfoByUserId(Long userId) {
        StandardProfileInfo info = userRepository.findStandardProfileInfoById(userId);
        return info == null ? null : info.withBaseUrl(baseUrl);
    }

    // OAuthInfo(oid, provider) -> userId(PK), role (토큰 서명용)
    public User findReaderUserByOAuthInfo(OAuthInfo oauthInfo) {
        Optional<User> readerUser = userRepository.findByOauthInfoProviderAndOauthInfoOid(oauthInfo.getProvider(), oauthInfo.getOid());
        if (readerUser.isPresent()) {
            return readerUser.get();
        } else {
            throw UnknownUserException.EXCEPTION;
        }
    }

    public boolean isUserPresentWithProviderAndOid(OAuthProvider provider, String oid) {
        Optional<User> readerUser = userRepository.findByOauthInfoProviderAndOauthInfoOid(provider, oid);
        return readerUser.isPresent();
    }

    public void checkNicknameDuplicate(String nickName) {
        if (isReservedNickName(nickName) || userRepository.existsByNickName(nickName)) {
            throw DuplicateNicknameException.EXCEPTION;
        }
    }

    public void checkNicknameDuplicateExceptSelf(String nickName, Long userId) {
        if (isReservedNickName(nickName) || userRepository.existsNickNameExceptSelf(nickName, userId)) {
            throw ProfileDuplicateNicknameException.EXCEPTION;
        }
    }

    // 예약 닉네임은 대소문자 구분 없이 차단 (예: "STORIX", "Storix", "storix")
    private boolean isReservedNickName(String nickName) {
        return nickName != null && STORIXStatic.RESERVED_NICK_NAMES.stream()
                .anyMatch(reserved -> reserved.equalsIgnoreCase(nickName));
    }

    // 개발자 회원 가입
    public AuthUserDetails saveDeveloperUser(CreateDeveloperUserCommand cmd) {
        try {
            User user = userRepository.save(cmd.toEntity());
            return new AuthUserDetails(user.getId(), user.getRole());
        } catch (DataIntegrityViolationException e) {
            throw DuplicateUserException.EXCEPTION;
        }
    }

    // 관리자 회원 가입
    public AuthUserDetails saveAdminUser(CreateAdminUserCommand cmd) {
        try {
            User user = userRepository.save(cmd.toEntity());
            return new AuthUserDetails(user.getId(), user.getRole());
        } catch (DataIntegrityViolationException e) {
            throw DuplicateUserException.EXCEPTION;
        }
    }

    public User findAdminByEmail(String email) {
        return userRepository.findByOauthInfoEmail(email)
                .orElseThrow(() -> UnknownUserException.EXCEPTION);
    }

    // 독자 회원 가입
    public AuthUserDetails saveReaderUser(CreateReaderUserCommand cmd) {
        try {
            User user = userRepository.save(cmd.toEntity());
            return new AuthUserDetails(user.getId(), user.getRole());
        } catch (DataIntegrityViolationException e) {
            throw DuplicateUserException.EXCEPTION;
        }
    }

    public User findUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw UnknownUserException.EXCEPTION;
        }
        return user.get();
    }

    public List<User> findUsersByIds(Collection<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    public Page<AdminUserListResponse> findAdminUsers(Long userId, String nickname, AccountState accountState, Pageable pageable) {
        return userRepository.searchAdminUsers(userId, nickname,accountState, pageable);
    }

    public void clearAllTitles() {
        userRepository.clearAllTitles();
    }

    public List<Long> findUntitledUserIdsHavingRawScore(Pageable pageable) {
        return userRepository.findUntitledUserIdsHavingRawScore(pageable);
    }

    public List<Long> findAdminNotificationTargetUserIds(Long lastUserId, LocalDateTime signupCutoff, Pageable pageable) {
        return userRepository.findAdminNotificationTargetUserIds(lastUserId, signupCutoff, pageable);
    }

    public Map<Long, StandardProfileInfo> findStandardProfileInfoByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 유저 프로필 정보
        List<StandardProfileInfo> profiles =
                userRepository.findStandardProfileInfoByUserIds(userIds);

        return profiles.stream()
                .map(info -> info.withBaseUrl(baseUrl))
                .collect(Collectors.toMap(
                        StandardProfileInfo::userId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // suspendedUntil 기준으로 정지 만료된 유저 일괄 복구 — ReportCase 상태와 독립적
    public int restoreExpiredSuspensions(LocalDateTime now) {
        return userRepository.restoreExpiredSuspensions(AccountState.SUSPENDED, now);
    }

    public int purgeOauthOidBefore(LocalDateTime cutoff) {
        return userRepository.purgeOauthOidBefore(cutoff);
    }

    public Map<Long, String> findNicknameMapByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userRepository.findNicknameInfoByUserIds(userIds).stream()
                .collect(Collectors.toMap(
                        UserNicknameInfo::userId,
                        UserNicknameInfo::nickName,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

}
