package com.storix.domain.domains.report.dto;

import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;

import java.time.LocalDateTime;

public record AdminReportSearchCondition(
        ReportTargetType targetType,
        ReportStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
