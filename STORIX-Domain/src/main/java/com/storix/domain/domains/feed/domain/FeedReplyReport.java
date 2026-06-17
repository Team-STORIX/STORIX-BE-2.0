package com.storix.domain.domains.feed.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "feed_reply_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_feed_report_reporter_reply",
                        columnNames = {"reporter_id", "reply_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedReplyReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterId;
    private Long reportedUserId;
    private Long replyId;

    @Column(name = "report_case_id")
    private Long reportCaseId;

    public FeedReplyReport(Long reporterId, Long reportedUserId, Long replyId) {
        this(reporterId, reportedUserId, replyId, null);
    }

    @Builder
    public FeedReplyReport(Long reporterId, Long reportedUserId, Long replyId, Long reportCaseId) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.replyId = replyId;
        this.reportCaseId = reportCaseId;
    }
}
