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

    // 발신자
    @Column(name = "sender", nullable = false, length = 50)
    private String sender;

    // 처리 시점
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;


    @Builder
    private UserHistory(Long userId, UserHistoryType historyType, String sender, LocalDateTime processedAt) {
        this.userId = userId;
        this.historyType = historyType;
        this.sender = sender;
        this.processedAt = processedAt;
    }
}
