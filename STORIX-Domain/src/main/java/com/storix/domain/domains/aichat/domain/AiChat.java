package com.storix.domain.domains.aichat.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 사용자당 하나를 이어서 쓴다
@Entity
@Getter
@Table(
        name = "ai_chats",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ai_chat_user", columnNames = {"user_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_chat_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    private AiChat(Long userId) {
        this.userId = userId;
    }

    public static AiChat of(Long userId) {
        return new AiChat(userId);
    }

    public void touch(LocalDateTime messageAt) {
        this.lastMessageAt = messageAt;
    }
}
