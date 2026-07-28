package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.WorksType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 행운의 작품 리스트
@Entity
@Getter
@Table(
        name = "event_story_card_lucky_works",
        indexes = @Index(name = "idx_story_card_lucky_work_genre_active", columnList = "genre, is_active"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_story_card_lucky_work_genre_title",
                columnNames = {"genre", "title"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryCardLuckyWork extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_card_lucky_work_id")
    private Long id;

    @Column(name = "genre", nullable = false, length = 20)
    private Genre genre;

    @Enumerated(EnumType.STRING)
    @Column(name = "works_type", nullable = false, length = 20)
    private WorksType worksType;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private Platform platform;

    @Column(name = "landing_url", length = 500)
    private String landingUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Builder
    public StoryCardLuckyWork(Genre genre,
                              WorksType worksType,
                              String title,
                              Platform platform,
                              String landingUrl,
                              boolean active) {
        this.genre = genre;
        this.worksType = worksType;
        this.title = title;
        this.platform = platform;
        this.landingUrl = landingUrl;
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }
}
