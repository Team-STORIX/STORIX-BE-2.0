package com.storix.domain.domains.report.dto;

import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.TargetContentType;

import java.time.LocalDateTime;

public record AdminReportSearchCondition(
        TargetContentType targetType,
        ReportStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Long reportedUserId
) {
}
