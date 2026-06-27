package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.AccountState;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserSummaryResponse(
        Long userId,
        String nickName,
        String email,
        LocalDateTime createdAt,
        AccountState accountState,
        LocalDateTime suspendedUntil,
        LocalDateTime lastLoginAt,
        AdminUserActivityStats activityStats,
        AdminUserReportStats reportStats,
        List<AdminUserSanctionHistoryResponse> sanctions
) {

    public static AdminUserSummaryResponse of(
            AdminUserBasicInfo basicInfo,
            AdminUserActivityStats activityStats,
            AdminUserReportStats reportStats,
            List<AdminUserSanctionHistoryResponse> sanctions
    ) {
        return new AdminUserSummaryResponse(
                basicInfo.userId(),
                basicInfo.nickName(),
                basicInfo.email(),
                basicInfo.createdAt(),
                basicInfo.accountState(),
                basicInfo.suspendedUntil(),
                basicInfo.lastLoginAt(),
                activityStats,
                reportStats,
                sanctions
        );
    }
}
