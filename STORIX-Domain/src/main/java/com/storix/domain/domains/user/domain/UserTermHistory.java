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
@Table(name = "user_term_history",
        indexes = {
                @Index(name = "idx_user_term_user", columnList = "user_id"),
                @Index(name = "idx_user_term_terms", columnList = "terms_id")
        }
)
public class UserTermHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 동의한 약관
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_term_history_terms"))
    private Terms terms;

    // 동의 여부
    @Column(name = "is_agreed", nullable = false)
    private boolean isAgreed;

    // 동의 시각
    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    // 철회 시각
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Builder
    private UserTermHistory(Long userId, Terms terms, boolean isAgreed,
                            LocalDateTime agreedAt, LocalDateTime withdrawnAt) {
        this.userId = userId;
        this.terms = terms;
        this.isAgreed = isAgreed;
        this.agreedAt = agreedAt;
        this.withdrawnAt = withdrawnAt;
    }

    // 동의 철회
    public void withdraw(LocalDateTime withdrawnAt) {
        this.isAgreed = false;
        this.withdrawnAt = withdrawnAt;
    }
}
