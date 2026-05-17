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

    // 수신자
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 알림 타입
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 32)
    private NotificationType notificationType;

    // 알림 클릭 시 이동할 타겟
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 16)
    private TargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "parent_target_id")
    private Long parentTargetId;

    // 본문
    @Column(nullable = false, length = 200)
    private String content;

    // 읽음 여부
    @Column(name = "is_read", nullable = false)
    private boolean isRead;


    /** 생성자 메서드 */
    @Builder
    public Notification(Long userId, NotificationType notificationType, TargetType targetType, Long targetId, Long parentTargetId, String content) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.targetType = targetType != null ? targetType : TargetType.NONE;
        this.targetId = targetId;
        this.parentTargetId = parentTargetId;
        this.content = content;
        this.isRead = false;
    }


    /** 비즈니스 메서드 */
    // 알림 읽음 처리
    public void read() {
        this.isRead = true;
    }
}
