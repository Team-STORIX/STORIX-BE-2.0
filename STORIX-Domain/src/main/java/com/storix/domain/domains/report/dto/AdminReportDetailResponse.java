package com.storix.domain.domains.report.dto;

import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.report.domain.ReportAction;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import com.storix.domain.domains.topicroom.domain.enums.ReportReason;

import java.time.LocalDateTime;
import java.util.List;

public record AdminReportDetailResponse(
        Long reportCaseId,
        ReportTargetType targetType,
        Long targetId,
        ReportStatus status,
        Long processedByAdminId,
        String processMemo,
        ReportAction processAction,
        LocalDateTime receivedAt,
        LocalDateTime processedAt,
        Summary summary,
        List<ReportItem> reports,
        ReportedContent reportedContent
) {

    public record Summary(
            Long reportedUserId,
            String reportedUserNickName,
            String location,
            ReportReason reason,
            String otherReason,
            long reportCount,
            LocalDateTime firstReportedAt
    ) {
    }

    public record ReportItem(
            Long reportId,
            Long reporterId,
            String reporterNickName,
            Long reportedUserId,
            ReportReason reason,
            String otherReason,
            LocalDateTime reportedAt
    ) {
    }

    public record ReportedContent(
            ReportTargetType targetType,
            Long targetId,
            Long parentTargetId,
            Long authorUserId,
            String authorNickName,
            String content,
            LocalDateTime createdAt,
            List<ReportedChatMessage> chatMessages
    ) {
    }

    public record ReportedChatMessage(
            Long messageId,
            Long senderId,
            String senderNickName,
            String message,
            MessageType messageType,
            LocalDateTime createdAt
    ) {
    }
}
