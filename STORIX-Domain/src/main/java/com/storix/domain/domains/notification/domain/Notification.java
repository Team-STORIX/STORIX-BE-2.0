package com.storix.domain.domains.notification.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(nullable = false)
    private String content;

    // 알림 클릭 시 이동할 타겟 id
    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    // 알림 읽음 처리
    public void read() {
        this.isRead = true;
    }

    @Builder
    public Notification(Long userId, NotificationType notificationType, String content, Long targetId) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.content = content;
        this.targetId = targetId;
        this.isRead = false;
    }
}
