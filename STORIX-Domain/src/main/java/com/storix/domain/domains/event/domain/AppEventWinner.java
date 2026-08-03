package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 확정된 당첨자만 남기는 테이블
@Entity
@Getter
@Table(
        name = "app_event_winners",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_app_event_winner_user",
                columnNames = {"app_event_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_app_event_winner_order", columnList = "app_event_id, draw_order")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppEventWinner extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_event_winner_id")
    private Long id;

    @Column(name = "app_event_id", nullable = false)
    private Long appEventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 뽑힌 순서 (1부터)
    @Column(name = "draw_order", nullable = false)
    private int drawOrder;

    @Builder
    public AppEventWinner(Long appEventId, Long userId, int drawOrder) {
        this.appEventId = appEventId;
        this.userId = userId;
        this.drawOrder = drawOrder;
    }
}
