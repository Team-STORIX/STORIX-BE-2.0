package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.OAuthProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminUserSummaryResponse(
        Long userId,
        String nickName,
        String email,
        OAuthProvider oauthProvider,
        LocalDate joinedAt,
        AccountState accountState,
        LocalDateTime suspendedUntil,
        LocalDateTime lastLoginAt,
        AdminUserActivityStats activityStats
) {

    public static AdminUserSummaryResponse of(
            AdminUserBasicInfo basicInfo,
            AdminUserActivityStats activityStats
    ) {
        return new AdminUserSummaryResponse(
                basicInfo.userId(),
                basicInfo.nickName(),
                basicInfo.email(),
                basicInfo.oauthProvider(),
                basicInfo.joinedAt(),
                basicInfo.accountState(),
                basicInfo.suspendedUntil(),
                basicInfo.lastLoginAt(),
                activityStats
        );
    }
}
