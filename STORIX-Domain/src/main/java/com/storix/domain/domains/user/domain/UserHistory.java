package com.storix.domain.domains.user.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_history",
        indexes = {
                @Index(name = "idx_user_created", columnList = "user_id, created_at DESC")
        }
)
public class UserHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "history_type", nullable = false, length = 32)
    private UserHistoryType historyType;

    // 처리자
    @Column(name = "processor", nullable = false, length = 50)
    private String processor;

    // 처리 시점
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    // 탈퇴 사유
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 32)
    private WithdrawReason reason;

    // 사용자 자유 입력
    @Column(name = "detail", length = 255)
    private String detail;


    @Builder
    private UserHistory(Long userId, UserHistoryType historyType, String processor,
                        LocalDateTime processedAt, WithdrawReason reason, String detail) {
        this.userId = userId;
        this.historyType = historyType;
        this.processor = processor;
        this.processedAt = processedAt;
        this.reason = reason;
        this.detail = detail;
    }
}
