package com.storix.domain.domains.user.service;

import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.adaptor.InternalSignupPendingAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.AdminSignupPending;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.dto.CreateAdminUserCommand;
import com.storix.domain.domains.user.exception.admin.AdminLoginFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final UserAdaptor userAdaptor;
    private final LibraryAdaptor libraryAdaptor;
    private final InternalSignupPendingAdaptor internalSignupPendingAdaptor;
    private final PasswordEncoder passwordEncoder;
    private final IssuedKeyGenerator issuedKeyGenerator;

    // 가입 요청 (Slack 승인 대기)
    public AdminSignupPending requestSignup(String email, String rawPassword, String nickName) {
        userAdaptor.checkNicknameDuplicate(nickName);

        String pendingId = issuedKeyGenerator.generatePendingId();

        AdminSignupPending pending = AdminSignupPending.builder()
                .pendingId(pendingId)
                .email(email)
                .encodedPassword(passwordEncoder.encode(rawPassword))
                .nickName(nickName)
                .ttl(600L) // 10분
                .build();

        internalSignupPendingAdaptor.save(pending);
        return pending;
    }

    // 승인 후 유저 생성
    @Transactional
    public AuthUserDetails approveAdminSignup(String pendingId) {
        AdminSignupPending pending = internalSignupPendingAdaptor.getAdminPending(pendingId);

        // Slack 승인까지 최대 10분 대기하는 동안 닉네임이 선점될 수 있어 재검증
        // (그래도 남는 마지막 순간의 경합은 uk_nick_name 제약이 최종 방어선)
        userAdaptor.checkNicknameDuplicate(pending.getNickName());

        CreateAdminUserCommand cmd = CreateAdminUserCommand.builder()
                .oid(pendingId)
                .email(pending.getEmail())
                .encodedPassword(pending.getEncodedPassword())
                .nickName(pending.getNickName())
                .build();

        AuthUserDetails authUserDetails = userAdaptor.saveAdminUser(cmd);

        libraryAdaptor.initLibrary(authUserDetails.getUserId());

        internalSignupPendingAdaptor.deleteAdminPending(pendingId);
        return authUserDetails;
    }

    // 로그인 (이메일 + 비밀번호 검증)
    @Transactional
    public AuthUserDetails loginAdmin(String email, String rawPassword) {
        User user;
        try {
            user = userAdaptor.findAdminByEmail(email);
        } catch (Exception e) {
            throw AdminLoginFailedException.EXCEPTION;
        }

        if (user.getPassword() == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw AdminLoginFailedException.EXCEPTION;
        }

        user.login();
        return new AuthUserDetails(user.getId(), user.getRole());
    }
}
