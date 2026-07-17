package com.storix.domain.domains.report.dto;

import com.storix.domain.domains.report.domain.ReportAction;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.TargetContentType;

import java.time.LocalDateTime;
import java.util.Set;

public record AdminReportListResponse(
        Long reportCaseId,
        TargetContentType targetType,
        Long targetId,
        Long reportedUserId,
        String reportedUserNickName,
        ReportStatus status,
        long reportCount,
        Long processedByAdminId,
        Set<ReportAction> processActions,
        LocalDateTime createdAt,
        LocalDateTime processedAt,
        boolean reopened
) {

    public static AdminReportListResponse from(ReportCase reportCase, long reportCount, String reportedUserNickName) {
        return new AdminReportListResponse(
                reportCase.getId(),
                reportCase.getTargetType(),
                reportCase.getTargetId(),
                reportCase.getReportedUserId(),
                reportedUserNickName,
                reportCase.getStatus(),
                reportCount,
                reportCase.getProcessedByAdminId(),
                reportCase.getProcessActions(),
                reportCase.getCreatedAt(),
                reportCase.getProcessedAt(),
                reportCase.hasPreviousProcessHistory()
        );
    }
}
