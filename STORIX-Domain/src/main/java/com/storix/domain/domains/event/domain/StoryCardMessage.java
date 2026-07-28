package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.domain.domains.works.domain.Genre;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "event_story_card_messages",
        indexes = @Index(name = "idx_story_card_message_genre_active", columnList = "genre, is_active"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_story_card_message_genre_content",
                columnNames = {"genre", "content"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryCardMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_card_message_id")
    private Long id;

    @Column(name = "genre", nullable = false, length = 20)
    private Genre genre;

    // 기획서의 '/' 구분 = 카드 위 줄바꿈. 개행 문자로 저장하고 응답에서 줄 단위로 쪼개 내려준다
    @Column(name = "content", nullable = false, length = 200)
    private String content;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Builder
    public StoryCardMessage(Genre genre, String content, boolean active) {
        this.genre = genre;
        this.content = content;
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }
}
