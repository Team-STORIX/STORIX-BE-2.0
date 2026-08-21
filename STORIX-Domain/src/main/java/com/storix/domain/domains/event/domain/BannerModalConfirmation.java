package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 배너 최초 안내 모달을 확인한 유저 기록
@Entity
@Getter
@Table(
        name = "event_banner_modal_confirmations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_banner_modal_confirmation_user_banner", columnNames = {"user_id", "banner_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BannerModalConfirmation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_modal_confirmation_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banner_id", nullable = false)
    private Banner banner;

    @Builder
    public BannerModalConfirmation(Long userId, Banner banner) {
        this.userId = userId;
        this.banner = banner;
    }
}
