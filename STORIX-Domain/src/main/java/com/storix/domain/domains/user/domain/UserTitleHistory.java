package com.storix.domain.domains.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "user_title_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_title_history",
                columnNames = {"user_id", "title"}
        ),
        indexes = {
                @Index(name = "idx_user_title_history_user", columnList = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTitleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_title_history_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "title", nullable = false, length = 40)
    private Title title;

    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;

    @Builder
    public UserTitleHistory(Long userId, Title title, LocalDateTime acquiredAt) {
        this.userId = userId;
        this.title = title;
        this.acquiredAt = acquiredAt;
    }

    public static UserTitleHistory of(Long userId, Title title, LocalDateTime acquiredAt) {
        return UserTitleHistory.builder()
                .userId(userId)
                .title(title)
                .acquiredAt(acquiredAt)
                .build();
    }
}
