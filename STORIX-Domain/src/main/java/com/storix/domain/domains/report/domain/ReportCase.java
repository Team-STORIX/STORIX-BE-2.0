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
                        name = "uk_report_case_target_reported_user",
                        columnNames = {"target_type", "target_id", "reported_user_id"}
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

    @Column(name = "reported_user_id", nullable = false)
    private Long reportedUserId;

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

    public void reopen() {
        this.status = ReportStatus.RECEIVED;
        // processAction / processMemo / processedByAdminId / processedAt 는 감사 이력으로 보존
    }

    /**
     * RECEIVED 상태인데 처리 이력이 남아있다면, 처리 완료 후 재신고로 reopen된 케이스다.
     * 화면에서 processAction/processedAt 등을 "현재 처리 결과"가 아닌 "이전 처리 이력"으로 구분 표시할 때 사용한다.
     */
    public boolean hasPreviousProcessHistory() {
        return status == ReportStatus.RECEIVED && processedAt != null;
    }

    public void process(ReportStatus status, ReportAction processAction, String processMemo, Long adminId) {
        this.status = status;
        this.processAction = processAction;
        this.processMemo = processMemo;
        this.processedByAdminId = adminId;
        this.processedAt = LocalDateTime.now();
    }

    @Builder
    public ReportCase(
            ReportTargetType targetType,
            Long targetId,
            Long reportedUserId,
            ReportStatus status,
            Long processedByAdminId,
            String processMemo,
            ReportAction processAction,
            LocalDateTime processedAt
    ) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.reportedUserId = reportedUserId;
        this.status = status != null ? status : ReportStatus.RECEIVED;
        this.processedByAdminId = processedByAdminId;
        this.processMemo = processMemo;
        this.processAction = processAction;
        this.processedAt = processedAt;
    }
}
