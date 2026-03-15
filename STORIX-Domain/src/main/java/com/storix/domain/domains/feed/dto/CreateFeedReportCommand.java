package com.storix.domain.domains.feed.dto;

import com.storix.domain.domains.feed.domain.FeedReplyReport;
import com.storix.domain.domains.feed.domain.FeedReport;

public record CreateFeedReportCommand (
        Long reporterId,
        Long reportedUserId,
        Long boardId
) {
    public FeedReport toEntity() {
        return new FeedReport(
                reporterId,
                reportedUserId,
                boardId
        );
    }
    public FeedReplyReport toReplyEntity() {
        return new FeedReplyReport(
                reporterId,
                reportedUserId,
                boardId
        );
    }
}
