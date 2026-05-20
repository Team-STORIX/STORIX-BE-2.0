package com.storix.domain.domains.report.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "report_case",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_report_case_target",
                        columnNames = {"target_type", "target_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportCase extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_case_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "processed_by_admin_id")
    private Long processedByAdminId;

    @Column(name = "process_memo", length = 500)
    private String processMemo;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_action", length = 30)
    private ReportAction processAction;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Builder
    public ReportCase(
            ReportTargetType targetType,
            Long targetId,
            ReportStatus status,
            Long processedByAdminId,
            String processMemo,
            ReportAction processAction,
            LocalDateTime processedAt
    ) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.status = status != null ? status : ReportStatus.RECEIVED;
        this.processedByAdminId = processedByAdminId;
        this.processMemo = processMemo;
        this.processAction = processAction;
        this.processedAt = processedAt;
    }
}
