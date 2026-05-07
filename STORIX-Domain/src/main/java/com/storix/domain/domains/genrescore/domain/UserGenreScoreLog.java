package com.storix.domain.domains.genrescore.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.domain.domains.genrescore.event.GenreScoreEvent;
import com.storix.domain.domains.genrescore.event.GenreScoreEventType;
import com.storix.domain.domains.works.domain.Genre;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_genre_score_log",
        indexes = {
                @Index(name = "idx_unprocessed", columnList = "processed_at, created_at"),
                @Index(name = "idx_user_event", columnList = "user_id, event_type, works_id")
        }
)
public class UserGenreScoreLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "works_id")
    private Long worksId;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false, length = 30)
    private Genre genre;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private GenreScoreEventType eventType;

    @Column(name = "weight", nullable = false)
    private int weight;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Builder
    private UserGenreScoreLog(Long userId, Long worksId, Genre genre, GenreScoreEventType eventType, int weight) {
        this.userId = userId;
        this.worksId = worksId;
        this.genre = genre;
        this.eventType = eventType;
        this.weight = weight;
    }

    public static UserGenreScoreLog from(GenreScoreEvent event) {
        return UserGenreScoreLog.builder()
                .userId(event.userId())
                .worksId(event.worksId())
                .genre(event.genre())
                .eventType(event.type())
                .weight(event.type().getWeight())
                .build();
    }
}
