package com.storix.domain.domains.adultverification.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.domain.domains.adultverification.exception.AlreadyProcessedAdultVerificationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "adult_verifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_adult_verification_identity_id",
                columnNames = "identity_verification_id"
        ),
        indexes = {
                @Index(name = "idx_adult_verification_user_status", columnList = "user_id, status"),
                // 만료 전환 배치 스캔용
                @Index(name = "idx_adult_verification_status_expires", columnList = "status, expires_at"),
                // 방치된 요청 정리용
                @Index(name = "idx_adult_verification_status_updated", columnList = "status, updated_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdultVerification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adult_verification_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 서버가 발급해 앱에 내려주는 포트원 본인인증 번호
    @Column(name = "identity_verification_id", nullable = false, length = 64)
    private String identityVerificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AdultVerificationStatus status;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // 이 날짜까지 유효. 시각은 보지 않는다
    @Column(name = "expires_at")
    private LocalDate expiresAt;

    // 포트원 pgTxId
    @Column(name = "provider_transaction_id", length = 128)
    private String providerTransactionId;


    /** 생성자 메서드 */
    @Builder
    private AdultVerification(Long userId, String identityVerificationId) {
        this.userId = userId;
        this.identityVerificationId = identityVerificationId;
        this.status = AdultVerificationStatus.PENDING;
    }

    public static AdultVerification from(Long userId, String identityVerificationId) {
        return AdultVerification.builder()
                .userId(userId)
                .identityVerificationId(identityVerificationId)
                .build();
    }


    /** 비즈니스 메서드 */
    // 확정은 조건부 UPDATE 로 한다. 이 메서드는 테스트 픽스처용
    public void verify(LocalDateTime verifiedAt, LocalDate expiresAt, String providerTransactionId) {
        if (!isConfirmable()) {
            throw AlreadyProcessedAdultVerificationException.EXCEPTION;
        }
        this.status = AdultVerificationStatus.VERIFIED;
        this.verifiedAt = verifiedAt;
        this.expiresAt = expiresAt;
        this.providerTransactionId = providerTransactionId;
    }

    public boolean isConfirmable() {
        return status.isConfirmable();
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isActiveAt(LocalDate today) {
        return status == AdultVerificationStatus.VERIFIED
                && expiresAt != null
                && !today.isAfter(expiresAt);
    }
}
