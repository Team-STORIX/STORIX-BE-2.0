package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "app_events")
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

    @Builder
    public AppEvent(String name,
                    String description,
                    LocalDateTime startAt,
                    LocalDateTime endAt,
                    boolean hasWinner,
                    Set<PromotionType> promotionTypes,
                    Long assigneeAdminId) {
        this.name = name;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.hasWinner = hasWinner;
        if (promotionTypes != null) {
            this.promotionTypes.addAll(promotionTypes);
        }
        this.assigneeAdminId = assigneeAdminId;
    }

    public void update(String name,
                       String description,
                       LocalDateTime startAt,
                       LocalDateTime endAt,
                       boolean hasWinner,
                       Set<PromotionType> promotionTypes) {
        this.name = name;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.hasWinner = hasWinner;

        this.promotionTypes.clear();
        if (promotionTypes != null) {
            this.promotionTypes.addAll(promotionTypes);
        }
    }

    public void endNow(LocalDateTime now) {
        this.endAt = now;
    }
}
