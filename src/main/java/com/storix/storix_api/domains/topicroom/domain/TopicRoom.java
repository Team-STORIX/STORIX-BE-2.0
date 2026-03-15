package com.storix.storix_api.domains.topicroom.domain;

import com.storix.storix_api.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "topic_room",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_topic_room_works_id",
                        columnNames = {"works_id"}
                )
        },
        indexes = {
                // 인기도 순 정렬 및 시간 조회를 위한 복합 인덱스
                @Index(name = "idx_popularity_last_chat", columnList = "popularity_score, last_chat_time")
        }
)
public class TopicRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_room_id")
    private Long id;

    @Column(name = "topic_room_name", nullable = false)
    private String topicRoomName;

    @Column(name = "works_id", nullable = false)
    private Long worksId;

    @Column(name = "active_user_number", nullable = false)
    private Integer activeUserNumber;

    @Column(name = "last_chat_time")
    private LocalDateTime lastChatTime;

    @Column(name = "popularity_score", nullable = false)
    private Double popularityScore;

    @Builder
    public TopicRoom(String topicRoomName, Long worksId) {
        this.topicRoomName = topicRoomName;
        this.worksId = worksId;
        this.activeUserNumber = 0;
        this.lastChatTime = LocalDateTime.now();
        this.popularityScore = 0.0;
    }


    // 인기도 점수 업데이트 메서드
    public void updatePopularityScore(double score) {
        this.popularityScore = score;
    }
}
