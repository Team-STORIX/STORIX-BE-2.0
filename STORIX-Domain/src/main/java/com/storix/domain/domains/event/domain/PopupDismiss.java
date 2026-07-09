package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(
        name = "event_popup_dismisses",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_popup_dismiss_user_popup", columnNames = {"user_id", "popup_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopupDismiss extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_popup_dismiss_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "popup_id", nullable = false)
    private Popup popup;

    // "오늘 안 보기" 날짜 (Asia/Seoul). 해당 날만 숨김.
    @Column(name = "dismissed_on", nullable = false)
    private LocalDate dismissedOn;

    @Builder
    public PopupDismiss(Long userId, Popup popup, LocalDate dismissedOn) {
        this.userId = userId;
        this.popup = popup;
        this.dismissedOn = dismissedOn;
    }
}
