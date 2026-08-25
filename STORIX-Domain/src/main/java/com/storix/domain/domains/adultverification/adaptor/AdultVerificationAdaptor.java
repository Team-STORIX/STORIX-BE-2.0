package com.storix.domain.domains.adultverification.adaptor;

import com.storix.domain.domains.adultverification.domain.AdultVerification;
import com.storix.domain.domains.adultverification.domain.AdultVerificationStatus;
import com.storix.domain.domains.adultverification.exception.UnknownAdultVerificationException;
import com.storix.domain.domains.adultverification.repository.AdultVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdultVerificationAdaptor {

    // 해제(REVOKED)는 만료가 아니라 미인증으로 보여준다
    private static final List<AdultVerificationStatus> SETTLED =
            List.of(AdultVerificationStatus.VERIFIED, AdultVerificationStatus.EXPIRED);

    private final AdultVerificationRepository adultVerificationRepository;

    /** 조회 작업 관련 메서드 */
    public AdultVerification getByIdentityVerificationId(String identityVerificationId) {
        return adultVerificationRepository.findByIdentityVerificationId(identityVerificationId)
                .orElseThrow(() -> UnknownAdultVerificationException.EXCEPTION);
    }

    public LocalDateTime findLatestVerifiedAtByUserId(Long userId) {
        return adultVerificationRepository.findLatestVerifiedAtByUserId(userId, AdultVerificationStatus.VERIFIED);
    }

    public Optional<AdultVerification> findLatestPending(Long userId) {
        return adultVerificationRepository.findFirstByUserIdAndStatusOrderByIdDesc(
                userId, AdultVerificationStatus.PENDING);
    }

    public Optional<AdultVerification> findLatestSettled(Long userId) {
        return adultVerificationRepository.findFirstByUserIdAndStatusInOrderByVerifiedAtDesc(userId, SETTLED);
    }

    /** 쓰기 작업 관련 메서드 */
    public AdultVerification save(AdultVerification adultVerification) {
        return adultVerificationRepository.save(adultVerification);
    }

    public int markVerifiedIfRetryable(String identityVerificationId,
                                       LocalDateTime verifiedAt,
                                       LocalDate expiresAt,
                                       String providerTransactionId) {
        return adultVerificationRepository.markVerifiedIfRetryable(
                identityVerificationId,
                AdultVerificationStatus.VERIFIED,
                AdultVerificationStatus.CONFIRMABLE,
                verifiedAt,
                expiresAt,
                providerTransactionId,
                LocalDateTime.now());
    }

    public int markReissued(String identityVerificationId) {
        return adultVerificationRepository.markReissued(
                identityVerificationId, AdultVerificationStatus.PENDING, LocalDateTime.now());
    }

    public int expireOverdue(LocalDate today) {
        return adultVerificationRepository.expireOverdue(
                AdultVerificationStatus.EXPIRED, AdultVerificationStatus.VERIFIED, today, LocalDateTime.now());
    }

    public int markAbandonedBefore(LocalDateTime threshold) {
        return adultVerificationRepository.markAbandonedBefore(
                AdultVerificationStatus.ABANDONED, AdultVerificationStatus.PENDING, threshold, LocalDateTime.now());
    }

    public int deleteAllByUserId(Long userId) {
        return adultVerificationRepository.deleteAllByUserId(userId);
    }
}
