package com.storix.domain.domains.adultverification.adaptor;

import com.storix.domain.domains.adultverification.domain.AdultVerification;
import com.storix.domain.domains.adultverification.domain.AdultVerificationStatus;
import com.storix.domain.domains.adultverification.dto.LatestVerifiedAt;
import com.storix.domain.domains.adultverification.exception.UnknownAdultVerificationException;
import com.storix.domain.domains.adultverification.repository.AdultVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Map<Long, LocalDateTime> findLatestVerifiedAtByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return adultVerificationRepository.findLatestVerifiedAtByUserIds(userIds, AdultVerificationStatus.VERIFIED)
                .stream()
                .collect(Collectors.toMap(LatestVerifiedAt::userId, LatestVerifiedAt::verifiedAt));
    }

    public Optional<AdultVerification> findLatestPending(Long userId) {
        return adultVerificationRepository.findFirstByUserIdAndStatusOrderByIdDesc(
                userId, AdultVerificationStatus.PENDING);
    }

    // 방치로 정리된 건도 본다. 확정이 유실된 뒤 배치가 지나갔어도 복구할 수 있어야 한다
    public Optional<AdultVerification> findLatestConfirmable(Long userId) {
        return adultVerificationRepository.findFirstByUserIdAndStatusInOrderByIdDesc(
                userId, AdultVerificationStatus.CONFIRMABLE);
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

    public List<AdultVerification> findAbandonCandidates(LocalDateTime threshold, int limit) {
        // 상한에 걸려 잘리면 다음 주기로 넘어가므로, 오래 밀린 건이 뒤로 밀리지 않게 정렬한다
        return adultVerificationRepository.findByStatusAndUpdatedAtBefore(
                AdultVerificationStatus.PENDING, threshold,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "updatedAt")));
    }

    public int markAbandoned(String identityVerificationId) {
        return adultVerificationRepository.markAbandoned(
                identityVerificationId,
                AdultVerificationStatus.ABANDONED,
                AdultVerificationStatus.PENDING,
                LocalDateTime.now());
    }

    public int deleteAllByUserId(Long userId) {
        return adultVerificationRepository.deleteAllByUserId(userId);
    }
}
