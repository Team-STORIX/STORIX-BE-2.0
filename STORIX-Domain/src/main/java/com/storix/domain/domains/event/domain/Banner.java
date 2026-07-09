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
        name = "event_banners",
        indexes = {
                @Index(name = "idx_event_banner_status_period", columnList = "status, display_start_at, display_end_at"),
                @Index(name = "idx_event_banner_app_event", columnList = "app_event_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_banner_id")
    private Long id;

    // 선택
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_event_id")
    private AppEvent appEvent;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_target_type", nullable = false, length = 20)
    private ContentTargetType contentTargetType;

    // 관리용
    @Column(name = "banner_title", nullable = false, length = 100)
    private String bannerTitle;

    @Column(name = "image_object_key", nullable = false, length = 512)
    private String imageObjectKey;

    @Column(name = "display_start_at", nullable = false)
    private LocalDateTime displayStartAt;

    @Column(name = "display_end_at", nullable = false)
    private LocalDateTime displayEndAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BannerStatus status;

    @Column(name = "assignee_admin_id")
    private Long assigneeAdminId;

    @Builder
    public Banner(AppEvent appEvent,
                  ContentTargetType contentTargetType,
                  String bannerTitle,
                  String imageObjectKey,
                  LocalDateTime displayStartAt,
                  LocalDateTime displayEndAt,
                  Long assigneeAdminId) {
        this.appEvent = appEvent;
        this.contentTargetType = contentTargetType;
        this.bannerTitle = bannerTitle;
        this.imageObjectKey = imageObjectKey;
        this.displayStartAt = displayStartAt;
        this.displayEndAt = displayEndAt;
        this.status = resolveStatus(displayStartAt, displayEndAt);
        this.assigneeAdminId = assigneeAdminId;
    }

    private static BannerStatus resolveStatus(LocalDateTime displayStartAt, LocalDateTime displayEndAt) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(displayStartAt)) {
            return BannerStatus.SCHEDULED;
        }
        if (now.isAfter(displayEndAt)) {
            return BannerStatus.ENDED;
        }
        return BannerStatus.ACTIVE;
    }

    public void update(ContentTargetType contentTargetType,
                       String bannerTitle,
                       String imageObjectKey,
                       LocalDateTime displayStartAt,
                       LocalDateTime displayEndAt) {
        this.contentTargetType = contentTargetType;
        this.bannerTitle = bannerTitle;
        this.imageObjectKey = imageObjectKey;
        // 강제 종료된 배너는 노출기간/상태를 수정으로 되살리지 않는다
        if (this.status != BannerStatus.ENDED) {
            this.displayStartAt = displayStartAt;
            this.displayEndAt = displayEndAt;
            this.status = resolveStatus(displayStartAt, displayEndAt);
        }
    }

    // 스케줄러: SCHEDULED → ACTIVE
    public void activate() {
        this.status = BannerStatus.ACTIVE;
    }

    // 스케줄러/강제 종료: ACTIVE → ENDED
    public void end() {
        this.status = BannerStatus.ENDED;
    }

    // 이벤트 기간으로 노출기간 clamp. 겹침 없으면 종료 처리.
    public void clampToEventPeriod(LocalDateTime eventStartAt, LocalDateTime eventEndAt) {
        if (this.status == BannerStatus.ENDED) {
            return;
        }
        LocalDateTime start = this.displayStartAt.isBefore(eventStartAt) ? eventStartAt : this.displayStartAt;
        LocalDateTime end = this.displayEndAt.isAfter(eventEndAt) ? eventEndAt : this.displayEndAt;
        if (!start.isBefore(end)) {
            this.status = BannerStatus.ENDED;
            return;
        }
        this.displayStartAt = start;
        this.displayEndAt = end;
        this.status = resolveStatus(start, end);
    }
}
