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
        name = "event_popups",
        indexes = {
                @Index(name = "idx_event_popup_status_period", columnList = "status, display_start_at, display_end_at"),
                @Index(name = "idx_event_popup_app_event", columnList = "app_event_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Popup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_popup_id")
    private Long id;

    // 선택
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_event_id")
    private AppEvent appEvent;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_target_type", nullable = false, length = 20)
    private ContentTargetType contentTargetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "exposure_policy", nullable = false, length = 20)
    private PopupExposurePolicy exposurePolicy;

    @Column(name = "popup_title", nullable = false, length = 100)
    private String popupTitle;

    @Column(name = "image_object_key", nullable = false, length = 512)
    private String imageObjectKey;

    @Column(name = "content", length = 500)
    private String content;

    @Column(name = "cta_text", length = 40)
    private String ctaText;

    @Column(name = "display_start_at", nullable = false)
    private LocalDateTime displayStartAt;

    @Column(name = "display_end_at", nullable = false)
    private LocalDateTime displayEndAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PopupStatus status;

    @Column(name = "assignee_admin_id")
    private Long assigneeAdminId;

    @Builder
    public Popup(AppEvent appEvent,
                      ContentTargetType contentTargetType,
                      PopupExposurePolicy exposurePolicy,
                      String popupTitle,
                      String imageObjectKey,
                      String content,
                      String ctaText,
                      LocalDateTime displayStartAt,
                      LocalDateTime displayEndAt,
                      Long assigneeAdminId) {
        this.appEvent = appEvent;
        this.contentTargetType = contentTargetType;
        this.exposurePolicy = exposurePolicy;
        this.popupTitle = popupTitle;
        this.imageObjectKey = imageObjectKey;
        this.content = content;
        this.ctaText = ctaText;
        this.displayStartAt = displayStartAt;
        this.displayEndAt = displayEndAt;
        this.status = resolveStatus(displayStartAt, displayEndAt);
        this.assigneeAdminId = assigneeAdminId;
    }

    private static PopupStatus resolveStatus(LocalDateTime displayStartAt, LocalDateTime displayEndAt) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(displayStartAt)) {
            return PopupStatus.SCHEDULED;
        }
        if (now.isAfter(displayEndAt)) {
            return PopupStatus.ENDED;
        }
        return PopupStatus.ACTIVE;
    }

    public void update(ContentTargetType contentTargetType,
                       PopupExposurePolicy exposurePolicy,
                       String popupTitle,
                       String imageObjectKey,
                       String content,
                       String ctaText,
                       LocalDateTime displayStartAt,
                       LocalDateTime displayEndAt) {
        this.contentTargetType = contentTargetType;
        this.exposurePolicy = exposurePolicy;
        this.popupTitle = popupTitle;
        this.imageObjectKey = imageObjectKey;
        this.content = content;
        this.ctaText = ctaText;
        this.displayStartAt = displayStartAt;
        this.displayEndAt = displayEndAt;
        // 강제 종료된 팝업은 수정으로 되살리지 않음
        if (this.status != PopupStatus.ENDED) {
            this.status = resolveStatus(displayStartAt, displayEndAt);
        }
    }

    // 스케줄러: SCHEDULED → ACTIVE
    public void activate() {
        this.status = PopupStatus.ACTIVE;
    }

    // 스케줄러/강제 종료: ACTIVE → ENDED
    public void end() {
        this.status = PopupStatus.ENDED;
    }

    // 이벤트 기간으로 노출기간 clamp. 겹침 없으면 종료 처리.
    public void clampToEventPeriod(LocalDateTime eventStartAt, LocalDateTime eventEndAt) {
        if (this.status == PopupStatus.ENDED) {
            return;
        }
        LocalDateTime start = this.displayStartAt.isBefore(eventStartAt) ? eventStartAt : this.displayStartAt;
        LocalDateTime end = this.displayEndAt.isAfter(eventEndAt) ? eventEndAt : this.displayEndAt;
        if (!start.isBefore(end)) {
            this.status = PopupStatus.ENDED;
            return;
        }
        this.displayStartAt = start;
        this.displayEndAt = end;
        this.status = resolveStatus(start, end);
    }
}
