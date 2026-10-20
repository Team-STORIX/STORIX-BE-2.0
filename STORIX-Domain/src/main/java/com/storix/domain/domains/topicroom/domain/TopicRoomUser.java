package com.storix.domain.domains.topicroom.domain;

import com.storix.domain.domains.topicroom.domain.enums.TopicRoomRole;
import com.storix.common.model.BaseTimeEntity;
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

    @Column(name = "last_read_message_id", nullable = false)
    private Long lastReadMessageId;

    @Column(name = "notification_enabled", nullable = false, columnDefinition = "tinyint(1) not null default 1")
    private boolean notificationEnabled;

    @Builder
    public TopicRoomUser(TopicRoom topicRoom, Long userId, TopicRoomRole role, Long lastReadMessageId) {
        this.topicRoom = topicRoom;
        this.userId = userId;
        this.role = role;
        this.notificationEnabled = true;
        // 들어오기 전 메시지는 안 읽은 수에 넣지 않는다
        this.lastReadMessageId = lastReadMessageId == null ? 0L : lastReadMessageId;
    }

    /** 비즈니스 메서드 */
    public void changeNotification(boolean enabled) {
        this.notificationEnabled = enabled;
    }
}