package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Getter
@Table(
        name = "app_events",
        // 같은 타입 이벤트의 기간 겹침 검사(SELECT ... FOR UPDATE)가 타는 인덱스.
        // 없으면 풀스캔이라 InnoDB가 테이블 전체에 락을 걸어 이벤트 생성/수정이 전부 직렬화된다
        indexes = @Index(name = "idx_app_event_type_period", columnList = "event_type, start_at, end_at")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_event_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private AppEventType eventType;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "has_winner", nullable = false)
    private boolean hasWinner;

    @ElementCollection(targetClass = PromotionType.class)
    @CollectionTable(
            name = "app_event_promotion_types",
            joinColumns = @JoinColumn(name = "app_event_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type", nullable = false, length = 20)
    @BatchSize(size = 100)
    private Set<PromotionType> promotionTypes = new HashSet<>();

    @Column(name = "assignee_admin_id")
    private Long assigneeAdminId;

    // 출석 이벤트 응모권 지급 기준: 누적 출석일 → 누적 지급 응모권. 비어 있으면 서비스의 기본 지급표를 사용한다.
    @ElementCollection
    @CollectionTable(
            name = "app_event_attendance_rewards",
            joinColumns = @JoinColumn(name = "app_event_id")
    )
    @MapKeyColumn(name = "attended_days")
    @Column(name = "cumulative_tickets", nullable = false)
    @BatchSize(size = 100)
    private Map<Integer, Integer> attendanceRewards = new HashMap<>();

    @Builder
    public AppEvent(String name,
                    String description,
                    AppEventType eventType,
                    LocalDateTime startAt,
                    LocalDateTime endAt,
                    boolean hasWinner,
                    Set<PromotionType> promotionTypes,
                    Map<Integer, Integer> attendanceRewards,
                    Long assigneeAdminId) {
        this.name = name;
        this.description = description;
        this.eventType = eventType == null ? AppEventType.GENERAL : eventType;
        this.startAt = startAt;
        this.endAt = endAt;
        this.hasWinner = hasWinner;
        if (promotionTypes != null) {
            this.promotionTypes.addAll(promotionTypes);
        }
        if (attendanceRewards != null) {
            this.attendanceRewards.putAll(attendanceRewards);
        }
        this.assigneeAdminId = assigneeAdminId;
    }

    public void update(String name,
                       String description,
                       AppEventType eventType,
                       LocalDateTime startAt,
                       LocalDateTime endAt,
                       boolean hasWinner,
                       Set<PromotionType> promotionTypes,
                       Map<Integer, Integer> attendanceRewards) {
        this.name = name;
        this.description = description;
        if (eventType != null) {
            this.eventType = eventType;
        }
        this.startAt = startAt;
        this.endAt = endAt;
        this.hasWinner = hasWinner;

        this.promotionTypes.clear();
        if (promotionTypes != null) {
            this.promotionTypes.addAll(promotionTypes);
        }

        this.attendanceRewards.clear();
        if (attendanceRewards != null) {
            this.attendanceRewards.putAll(attendanceRewards);
        }
    }

    public void endNow(LocalDateTime now) {
        this.endAt = now;
    }

    public boolean isActiveAt(LocalDateTime now) {
        return !now.isBefore(startAt) && now.isBefore(endAt);
    }

    // end_at은 exclusive 경계라 그 시점부터 종료로 본다
    public boolean isEndedAt(LocalDateTime now) {
        return !now.isBefore(endAt);
    }

    // 이벤트 종류별 기준 시각(출석 자정 / 스토리 카드 06:00)으로 계산한 서비스 날짜
    public LocalDate serviceDateOf(LocalDateTime now) {
        return eventType.serviceDateOf(now);
    }

    // 참여 가능한 첫 날. end_at은 exclusive 경계라 마지막 날은 그 직전 시점 기준으로 계산한다
    public LocalDate participationStartDate() {
        return eventType.firstParticipableDate(startAt);
    }

    public LocalDate participationEndDate() {
        return eventType.lastParticipableDate(endAt);
    }
}
