package com.storix.domain.domains.topicroom.domain;

import com.storix.domain.domains.topicroom.domain.enums.ReportReason;
import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "topic_room_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_topic_room_report_reporter_reported_room",
                        columnNames = {"reporter_id", "reported_user_id", "topic_room_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopicRoomReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterId;
    private Long reportedUserId;
    private Long topicRoomId;
    private Long chatMessageId;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Column(length = 100)
    private String otherReason;

    @Column(name = "report_case_id")
    private Long reportCaseId;

    public TopicRoomReport(Long reporterId, Long reportedUserId, Long topicRoomId, Long chatMessageId, ReportReason reason, String otherReason) {
        this(reporterId, reportedUserId, topicRoomId, chatMessageId, reason, otherReason, null);
    }

    @Builder
    public TopicRoomReport(Long reporterId, Long reportedUserId, Long topicRoomId, Long chatMessageId, ReportReason reason, String otherReason, Long reportCaseId) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.topicRoomId = topicRoomId;
        this.chatMessageId = chatMessageId;
        this.reason = reason;
        this.otherReason = otherReason;
        this.reportCaseId = reportCaseId;
    }
}
