package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.AccountState;

import java.time.LocalDateTime;

public record AdminUserListResponse(
        Long userId,
        String nickName,
        String email,
        LocalDateTime createdAt,
        AccountState accountState,
        LocalDateTime suspendedUntil,
        LocalDateTime lastLoginAt
) {
}
