package com.storix.domain.domains.report.dto;

import java.util.List;

public record AdminUserReportSummaryResponse(
        Long userId,
        String nickName,
        long totalCaseCount,
        long receivedCount,
        long completedCount,
        long rejectedCount,
        List<AdminReportListResponse> recentCases
) {
}
