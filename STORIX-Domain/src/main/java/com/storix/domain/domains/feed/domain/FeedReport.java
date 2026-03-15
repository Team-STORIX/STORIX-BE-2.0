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
        name = "feed_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_feed_report_reporter_board",
                        columnNames = {"reporter_id", "board_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterId;
    private Long reportedUserId;
    private Long boardId;

    @Builder
    public FeedReport(Long reporterId, Long reportedUserId, Long boardId) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.boardId = boardId;
    }
}