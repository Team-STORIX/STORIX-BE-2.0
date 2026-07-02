package com.storix.domain.domains.report.dto;

import com.storix.domain.domains.report.domain.ReportAction;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.TargetContentType;

import java.time.LocalDateTime;

public record AdminReportListResponse(
        Long reportCaseId,
        TargetContentType targetType,
        Long targetId,
        Long reportedUserId,
        String reportedUserNickName,
        ReportStatus status,
        long reportCount,
        Long processedByAdminId,
        ReportAction processAction,
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
                reportCase.getProcessAction(),
                reportCase.getCreatedAt(),
                reportCase.getProcessedAt(),
                reportCase.hasPreviousProcessHistory()
        );
    }
}
