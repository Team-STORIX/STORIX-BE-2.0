package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.User;

import java.time.LocalDateTime;

public record AdminUserBasicInfo(
        Long userId,
        String nickName,
        String email,
        LocalDateTime createdAt,
        AccountState accountState,
        LocalDateTime suspendedUntil,
        LocalDateTime lastLoginAt
) {

    public static AdminUserBasicInfo from(User user) {
        return new AdminUserBasicInfo(
                user.getId(),
                user.getNickName(),
                user.getOauthInfo() == null ? null : user.getOauthInfo().getEmail(),
                user.getCreatedAt(),
                user.getAccountState(),
                user.getSuspendedUntil(),
                user.getLastLoginAt()
        );
    }
}
