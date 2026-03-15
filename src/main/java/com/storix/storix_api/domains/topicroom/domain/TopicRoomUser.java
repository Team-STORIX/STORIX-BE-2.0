package com.storix.storix_api.domains.topicroom.domain;

import com.storix.storix_api.domains.topicroom.domain.enums.TopicRoomRole;
import com.storix.storix_api.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "topic_room_user",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_topic_room_user_id",
                        columnNames = { "user_id", "topic_room_id" }
                )
        }
)
public class TopicRoomUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_room_user_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_room_id")
    private TopicRoom topicRoom;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private TopicRoomRole role;

    @Builder
    public TopicRoomUser(TopicRoom topicRoom, Long userId, TopicRoomRole role) {
        this.topicRoom = topicRoom;
        this.userId = userId;
        this.role = role;
    }
}