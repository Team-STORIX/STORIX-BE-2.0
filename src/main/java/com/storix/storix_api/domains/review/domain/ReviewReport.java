package com.storix.storix_api.domains.review.domain;

import com.storix.storix_api.domains.topicroom.domain.enums.ReportReason;
import com.storix.storix_api.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "review_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_report_reporter_review",
                        columnNames = {"reporter_id", "review_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterId;
    private Long reportedUserId;
    private Long reviewId;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Column(length = 100)
    private String otherReason;

    @Builder
    public ReviewReport(Long reporterId, Long reportedUserId, Long boardId, ReportReason reason, String otherReason) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reviewId = boardId;
        this.reason = reason;
        this.otherReason = otherReason;
    }
}