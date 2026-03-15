package com.storix.storix_api.domains.topicroom.domain;

import com.storix.storix_api.domains.topicroom.domain.enums.ReportReason;
import com.storix.storix_api.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopicRoomReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterId;
    private Long reportedUserId;
    private Long topicRoomId;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Column(length = 100)
    private String otherReason;

    @Builder
    public TopicRoomReport(Long reporterId, Long reportedUserId, Long topicRoomId, ReportReason reason, String otherReason) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.topicRoomId = topicRoomId;
        this.reason = reason;
        this.otherReason = otherReason;
    }
}