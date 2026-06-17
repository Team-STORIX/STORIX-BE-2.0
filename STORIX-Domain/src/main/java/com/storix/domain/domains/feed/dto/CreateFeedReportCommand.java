package com.storix.domain.domains.feed.dto;

import com.storix.domain.domains.feed.domain.FeedReplyReport;
import com.storix.domain.domains.feed.domain.FeedReport;

public record CreateFeedReportCommand (
        Long reporterId,
        Long reportedUserId,
        Long targetId,
        Long reportCaseId
) {
    public CreateFeedReportCommand(Long reporterId, Long reportedUserId, Long targetId) {
        this(reporterId, reportedUserId, targetId, null);
    }

    public FeedReport toEntity() {
        return new FeedReport(
                reporterId,
                reportedUserId,
                targetId,
                reportCaseId
        );
    }
    public FeedReplyReport toReplyEntity() {
        return new FeedReplyReport(
                reporterId,
                reportedUserId,
                targetId,
                reportCaseId
        );
    }
}
