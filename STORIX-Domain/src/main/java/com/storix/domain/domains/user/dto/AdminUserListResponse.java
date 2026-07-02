package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.OAuthProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminUserListResponse(
        Long userId,
        String nickName,
        String email,
        OAuthProvider oauthProvider,
        LocalDate joinedAt,
        AccountState accountState,
        LocalDateTime suspendedUntil,
        LocalDateTime lastLoginAt,
        Long reportedCount
) {

    public AdminUserListResponse(
            Long userId,
            String nickName,
            String email,
            OAuthProvider oauthProvider,
            LocalDateTime createdAt,
            AccountState accountState,
            LocalDateTime suspendedUntil,
            LocalDateTime lastLoginAt
    ) {
        this(userId, nickName, email, oauthProvider, createdAt.toLocalDate(), accountState, suspendedUntil, lastLoginAt, 0L);
    }
}
