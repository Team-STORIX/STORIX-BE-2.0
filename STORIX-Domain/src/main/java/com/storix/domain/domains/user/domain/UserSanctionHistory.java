package com.storix.domain.domains.user.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_sanction_history",
        indexes = {
                @Index(name = "idx_user_sanction_user_created", columnList = "user_id, created_at DESC"),
                @Index(name = "idx_user_sanction_report_case", columnList = "report_case_id")
        }
)
public class UserSanctionHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_sanction_history_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "admin_id")
    private Long adminId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private UserSanctionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private UserSanctionSource source;

    @Column(name = "report_case_id")
    private Long reportCaseId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "memo", length = 500)
    private String memo;

    @Builder
    private UserSanctionHistory(
            Long userId,
            Long adminId,
            UserSanctionType type,
            UserSanctionSource source,
            Long reportCaseId,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            String memo
    ) {
        this.userId = userId;
        this.adminId = adminId;
        this.type = type;
        this.source = source;
        this.reportCaseId = reportCaseId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.memo = memo;
    }
}
