package com.storix.domain.domains.user.dto;

public record AdminUserReportStats(
        long reporterCount,
        long reportedCount
) {
}
