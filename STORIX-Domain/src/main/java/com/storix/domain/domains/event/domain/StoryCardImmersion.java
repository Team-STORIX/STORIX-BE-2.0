package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오늘의 몰입력. 장르와 무관하게 전체 풀에서 완전 랜덤으로 뽑힌다.
 */
@Entity
@Getter
@Table(
        name = "event_story_card_immersions",
        indexes = @Index(name = "idx_story_card_immersion_active", columnList = "is_active"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_story_card_immersion_content",
                columnNames = {"content"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryCardImmersion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_card_immersion_id")
    private Long id;

    @Column(name = "content", nullable = false, length = 100)
    private String content;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Builder
    public StoryCardImmersion(String content, boolean active) {
        this.content = content;
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }
}
