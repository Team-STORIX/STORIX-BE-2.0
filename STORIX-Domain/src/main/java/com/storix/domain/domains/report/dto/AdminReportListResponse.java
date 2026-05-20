package com.storix.domain.domains.report.dto;

import com.storix.domain.domains.report.domain.ReportAction;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;

import java.time.LocalDateTime;

public record AdminReportListResponse(
        Long reportCaseId,
        ReportTargetType targetType,
        Long targetId,
        ReportStatus status,
        long reportCount,
        Long processedByAdminId,
        ReportAction processAction,
        LocalDateTime createdAt,
        LocalDateTime processedAt
) {

    public static AdminReportListResponse from(ReportCase reportCase, long reportCount) {
        return new AdminReportListResponse(
                reportCase.getId(),
                reportCase.getTargetType(),
                reportCase.getTargetId(),
                reportCase.getStatus(),
                reportCount,
                reportCase.getProcessedByAdminId(),
                reportCase.getProcessAction(),
                reportCase.getCreatedAt(),
                reportCase.getProcessedAt()
        );
    }
}
