package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.report.domain.ReportAction;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import com.storix.domain.domains.topicroom.domain.enums.ReportReason;

import java.time.LocalDateTime;

public record AdminUserReportItemResponse(
        Long reportId,
        AdminUserReportDirection direction,
        Long reportCaseId,
        ReportTargetType targetType,
        Long targetId,
        Long reporterId,
        Long reportedUserId,
        ReportReason reason,
        String otherReason,
        ReportStatus status,
        ReportAction processAction,
        LocalDateTime createdAt
) {
}
