package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "user_app_events",
        indexes = {
                @Index(name = "idx_user_app_event_user_status", columnList = "user_id, status, user_app_event_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAppEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_app_event_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private UserAppEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserAppEventStatus status;

    @Column(name = "payload_json", length = 1000)
    private String payloadJson;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "acked_at")
    private LocalDateTime ackedAt;

    @Builder
    public UserAppEvent(Long userId,
                        UserAppEventType eventType,
                        String payloadJson,
                        LocalDateTime occurredAt) {
        this.userId = userId;
        this.eventType = eventType;
        this.status = UserAppEventStatus.PENDING;
        this.payloadJson = payloadJson;
        this.occurredAt = occurredAt;
    }

    public void ack() {
        this.status = UserAppEventStatus.ACK;
        this.ackedAt = LocalDateTime.now();
    }
}
