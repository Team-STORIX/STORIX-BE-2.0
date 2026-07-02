package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminUserBasicInfo(
        Long userId,
        String nickName,
        String email,
        OAuthProvider oauthProvider,
        LocalDate joinedAt,
        AccountState accountState,
        LocalDateTime suspendedUntil,
        LocalDateTime lastLoginAt
) {

    public static AdminUserBasicInfo from(User user) {
        return new AdminUserBasicInfo(
                user.getId(),
                user.getNickName(),
                user.getOauthInfo() == null ? null : user.getOauthInfo().getEmail(),
                user.getOauthInfo() == null ? null : user.getOauthInfo().getProvider(),
                user.getCreatedAt().toLocalDate(),
                user.getAccountState(),
                user.getSuspendedUntil(),
                user.getLastLoginAt()
        );
    }
}
