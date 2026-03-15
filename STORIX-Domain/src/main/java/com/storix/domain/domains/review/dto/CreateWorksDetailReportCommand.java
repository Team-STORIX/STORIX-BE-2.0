package com.storix.domain.domains.review.dto;

import com.storix.domain.domains.review.domain.ReviewReport;
import com.storix.domain.domains.topicroom.domain.enums.ReportReason;

public record CreateWorksDetailReportCommand(
        Long reporterId,
        Long reportedUserId,
        Long reviewId,
        ReportReason reason,
        String otherReason
) {
    public ReviewReport toEntity() {
        return new ReviewReport(
                reporterId,
                reportedUserId,
                reviewId,
                reason,
                otherReason
        );
    }
}
