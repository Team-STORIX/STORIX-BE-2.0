package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 앱 이벤트 상세 웹페이지의 최초 안내 모달을 확인한 유저 기록. 프로모션 수단(팝업/배너) 무관, appEventId로만 구분한다
@Entity
@Getter
@Table(
        name = "event_page_modal_confirmations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_page_modal_confirmation_user_event", columnNames = {"user_id", "app_event_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppEventPageModalConfirmation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_modal_confirmation_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "app_event_id", nullable = false)
    private Long appEventId;

    @Builder
    public AppEventPageModalConfirmation(Long userId, Long appEventId) {
        this.userId = userId;
        this.appEventId = appEventId;
    }
}
