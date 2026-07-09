package com.storix.domain.domains.topicroom.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.domain.domains.chat.domain.MessageType;
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
                @Index(name = "idx_popularity_last_chat", columnList = "popularity_score, last_chat_time"),
                @Index(name = "idx_growth_rate", columnList = "popularity_growth_rate"),
                @Index(name = "idx_active_user_last_chat", columnList = "active_user_number, last_chat_time"),
                @Index(name = "idx_activity_score_last_chat", columnList = "activity_score, last_chat_time")
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

    @Column(name = "previous_active_user_number", nullable = false)
    private Integer previousActiveUserNumber;

    @Column(name = "last_chat_time")
    private LocalDateTime lastChatTime;

    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_message_type")
    private MessageType lastMessageType;

    @Column(name = "last_message_sender_id")
    private Long lastMessageSenderId;

    @Column(name = "popularity_score", nullable = false)
    private Double popularityScore;

    @Column(name = "popularity_growth_rate", nullable = false)
    private Double popularityGrowthRate;

    @Column(name = "activity_score", nullable = false)
    private Double activityScore;

    @Builder
    public TopicRoom(String topicRoomName, Long worksId) {
        this.topicRoomName = topicRoomName;
        this.worksId = worksId;
        this.activeUserNumber = 0;
        this.previousActiveUserNumber = 0;
        this.lastChatTime = LocalDateTime.now();
        this.lastMessage = null;
        this.lastMessageType = null;
        this.lastMessageSenderId = null;
        this.popularityScore = 0.0;
        this.popularityGrowthRate = 0.0;
        this.activityScore = 0.0;
    }

    // 인기도 점수 업데이트
    public void updatePopularityScore(double score) {
        this.popularityScore = score;
    }

    // 증가율 업데이트
    public void updatePopularityGrowthRate(double rate) {
        this.popularityGrowthRate = rate;
    }

    // 참여자 수 스냅샷
    public void snapshotActiveUserNumber() {
        this.previousActiveUserNumber = this.activeUserNumber;
    }
}
