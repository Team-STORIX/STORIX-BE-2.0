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

    // 앱 외부 URL
    @Column(name = "link", length = 1000)
    private String targetLink;

    // 본문
    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 200)
    private String content;

    // 읽음 여부
    @Column(name = "is_read", nullable = false)
    private boolean isRead;


    /** 생성자 메서드 */
    @Builder
    public Notification(Long userId, NotificationType notificationType, TargetType targetType, Long targetId, Long parentTargetId, String title, String content) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.targetType = targetType != null ? targetType : TargetType.NONE;
        this.targetId = targetId;
        this.parentTargetId = parentTargetId;
        this.title = title;
        this.content = content;
        this.isRead = false;
    }


    // 운영자 브로드캐스트용 (이동 X)
    public static Notification ofBroadcast(Long userId, NotificationType notificationType, String title, String content) {
        return Notification.builder()
                .userId(userId)
                .notificationType(notificationType)
                .targetType(TargetType.NONE)
                .title(title)
                .content(content)
                .build();
    }

    // 운영자 브로드캐스트용 (앱 이벤트)
    public static Notification ofEventBroadcast(Long userId, NotificationType notificationType, String title, String content, Long eventTargetId) {
        return Notification.builder()
                .userId(userId)
                .notificationType(notificationType)
                .targetType(TargetType.APP_EVENT)
                .targetId(eventTargetId)
                .title(title)
                .content(content)
                .build();
    }

    // 운영자 브로드캐스트용 (외부 링크)
    public static Notification ofExternalBroadcast(Long userId, NotificationType notificationType, String title, String content, String targetLink) {
        Notification notification = Notification.builder()
                .userId(userId)
                .notificationType(notificationType)
                .targetType(TargetType.EXTERNAL)
                .title(title)
                .content(content)
                .build();
        notification.targetLink = targetLink;
        return notification;
    }


    /** 비즈니스 메서드 */
    // 알림 읽음 처리
    public void read() {
        this.isRead = true;
    }
}
