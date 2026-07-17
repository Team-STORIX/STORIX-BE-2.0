package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "app_event_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_app_event_participant_user",
                columnNames = {"app_event_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_app_event_participant_winner", columnList = "app_event_id, is_winner, user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppEventParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_event_participant_id")
    private Long id;

    @Column(name = "app_event_id", nullable = false)
    private Long appEventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_winner", nullable = false)
    private boolean winner;

    @Builder
    public AppEventParticipant(Long appEventId, Long userId, boolean winner) {
        this.appEventId = appEventId;
        this.userId = userId;
        this.winner = winner;
    }

    public void markWinner() {
        this.winner = true;
    }
}
