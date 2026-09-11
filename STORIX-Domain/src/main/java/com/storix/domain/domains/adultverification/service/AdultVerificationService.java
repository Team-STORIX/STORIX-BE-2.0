package com.storix.domain.domains.adultverification.service;

import com.storix.common.property.PortOneProperties;
import com.storix.domain.domains.adultverification.adaptor.AdultVerificationAdaptor;
import com.storix.domain.domains.adultverification.domain.AdultAgePolicy;
import com.storix.domain.domains.adultverification.domain.AdultVerification;
import com.storix.domain.domains.adultverification.domain.AdultVerificationPolicy;
import com.storix.domain.domains.adultverification.dto.AdultVerificationStatusInfo;
import com.storix.domain.domains.adultverification.dto.AdultVerificationTicket;
import com.storix.domain.domains.adultverification.dto.IdentityVerificationResult;
import com.storix.domain.domains.adultverification.exception.AdultVerificationOwnerMismatchException;
import com.storix.domain.domains.adultverification.exception.AlreadyAdultVerifiedException;
import com.storix.domain.domains.adultverification.exception.ExpiredOrRevokedAdultVerificationException;
import com.storix.domain.domains.adultverification.exception.IdentityVerificationProviderException;
import com.storix.domain.domains.adultverification.exception.IncompleteIdentityVerificationException;
import com.storix.domain.domains.adultverification.exception.MinorNotAllowedException;
import com.storix.domain.domains.adultverification.exception.MissingBirthDateException;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdultVerificationService {

    private static final String IDENTITY_VERIFICATION_ID_PREFIX = "identity-verification-";

    private final AdultVerificationAdaptor adultVerificationAdaptor;
    private final UserAdaptor userAdaptor;
    private final PortOneProperties portOneProperties;

    // 확정을 기다리는 티켓. 방치 정리된 건은 배치가 이미 포트원과 맞췄으므로 여기서는 보지 않는다
    @Transactional(readOnly = true)
    public String findPendingIdentityVerificationId(Long userId) {
        return adultVerificationAdaptor.findLatestPending(userId)
                .map(AdultVerification::getIdentityVerificationId)
                .orElse(null);
    }

    // 아직 확정되지 않은 최신 건. 발급 전에 포트원 상태를 물어보려면 부르는 쪽이 먼저 알아야 한다
    // 방치로 정리된 건까지 보는 이유는, 확정이 유실된 뒤 배치가 지나가도 복구할 수 있어야 해서다
    @Transactional(readOnly = true)
    public String findConfirmableIdentityVerificationId(Long userId) {
        return adultVerificationAdaptor.findLatestConfirmable(userId)
                .map(AdultVerification::getIdentityVerificationId)
                .orElse(null);
    }

    // 통합인증은 인증 성공 건당 과금이라, 이미 유효한 유저는 창을 띄우기 전에 막는다
    @Transactional
    public AdultVerificationTicket issue(Long userId) {
        userAdaptor.findUserByIdForUpdate(userId);

        LocalDateTime verifiedAt = adultVerificationAdaptor.findLatestVerifiedAtByUserId(userId);
        if (AdultVerificationPolicy.isValidOn(verifiedAt, LocalDate.now())) {
            throw AlreadyAdultVerifiedException.EXCEPTION;
        }

        // 확정을 기다리는 티켓이 있으면 그대로 준다
        String identityVerificationId = adultVerificationAdaptor.findLatestPending(userId)
                .map(AdultVerification::getIdentityVerificationId)
                .map(this::reissue)
                .orElseGet(() -> issueNew(userId));

        return new AdultVerificationTicket(
                identityVerificationId,
                portOneProperties.getStoreId(),
                portOneProperties.getChannelKey()
        );
    }

    private String reissue(String identityVerificationId) {
        adultVerificationAdaptor.markReissued(identityVerificationId);
        return identityVerificationId;
    }

    private String issueNew(Long userId) {
        String identityVerificationId = IDENTITY_VERIFICATION_ID_PREFIX + UUID.randomUUID();
        adultVerificationAdaptor.save(AdultVerification.from(userId, identityVerificationId));

        log.info("성인인증 티켓 신규 발급 userId={} identityVerificationId={}", userId, identityVerificationId);
        return identityVerificationId;
    }

    // 포트원에 묻기 전에 거른다. 확정된 건이면 그 결과를, 아직이면 null 을 돌려준다
    @Transactional(readOnly = true)
    public AdultVerificationStatusInfo findConfirmed(Long userId, String identityVerificationId) {
        AdultVerification adultVerification = adultVerificationAdaptor.getByIdentityVerificationId(identityVerificationId);
        if (!adultVerification.isOwnedBy(userId)) {
            throw AdultVerificationOwnerMismatchException.EXCEPTION;
        }
        if (adultVerification.isConfirmable()) {
            return null;
        }
        // 만료·해제된 건은 되살리지 않는다
        if (!adultVerification.isActiveAt(LocalDate.now())) {
            throw ExpiredOrRevokedAdultVerificationException.EXCEPTION;
        }
        return AdultVerificationStatusInfo.verified(
                adultVerification.getVerifiedAt(), adultVerification.getExpiresAt());
    }

    // findConfirmed 를 통과하고, result 는 서버가 포트원에 직접 물어 받은 것이어야 한다
    @Transactional
    public AdultVerificationStatusInfo confirm(Long userId,
                                               String identityVerificationId,
                                               IdentityVerificationResult result) {
        // 요청한 건의 결과가 맞는지부터 본다. 다르면 공급사나 중간 계층 이상이다
        if (!identityVerificationId.equals(result.identityVerificationId())) {
            log.error("포트원 본인인증 응답 불일치 requested={} responded={}",
                    identityVerificationId, result.identityVerificationId());
            throw IdentityVerificationProviderException.EXCEPTION;
        }
        if (!result.isVerified()) {
            log.warn("성인인증 확정 불가 identityVerificationId={} status={} reason={} pgCode={} pgMessage={}",
                    identityVerificationId, result.status(),
                    result.failureReason(), result.failurePgCode(), result.failurePgMessage());
            throw IncompleteIdentityVerificationException.EXCEPTION;
        }
        // 스펙상 VERIFIED 면 반드시 있는 값들이다. 없으면 공급사 이상이라 재시도 여지를 남긴다
        if (result.birthDate() == null) {
            log.error("포트원 본인인증 응답 필드 누락 identityVerificationId={} field=birthDate", identityVerificationId);
            throw MissingBirthDateException.EXCEPTION;
        }
        if (result.verifiedAt() == null) {
            log.error("포트원 본인인증 응답 필드 누락 identityVerificationId={} field=verifiedAt", identityVerificationId);
            throw IdentityVerificationProviderException.EXCEPTION;
        }
        if (!AdultAgePolicy.isAdult(result.birthDate(), LocalDate.now())) {
            log.warn("성인인증 미성년 차단 userId={} identityVerificationId={}", userId, identityVerificationId);
            throw MinorNotAllowedException.EXCEPTION;
        }

        // 유저 행을 잠가 확정을 직렬화한다. 동시에 들어온 두 티켓이 둘 다 통과하는 걸 막는다
        userAdaptor.findUserByIdForUpdate(userId);

        // 앞선 검사는 포트원 호출을 아끼는 용도고, 경합까지 막는 건 여기다
        AdultVerification adultVerification = adultVerificationAdaptor.getByIdentityVerificationId(identityVerificationId);
        if (!adultVerification.isConfirmable()) {
            if (!adultVerification.isActiveAt(LocalDate.now())) {
                throw ExpiredOrRevokedAdultVerificationException.EXCEPTION;
            }
            return AdultVerificationStatusInfo.verified(
                    adultVerification.getVerifiedAt(), adultVerification.getExpiresAt());
        }

        if (AdultVerificationPolicy.isValidOn(
                adultVerificationAdaptor.findLatestVerifiedAtByUserId(userId), LocalDate.now())) {
            throw AlreadyAdultVerifiedException.EXCEPTION;
        }

        LocalDateTime verifiedAt = result.verifiedAt();
        LocalDate expiresAt = AdultVerificationPolicy.expiresOn(verifiedAt);

        int updated = adultVerificationAdaptor.markVerifiedIfRetryable(
                identityVerificationId, verifiedAt, expiresAt, result.providerTransactionId());
        // 유저 행을 잡고 있어 운영에서는 오지 않는다. 락을 타지 않는 테스터 이력 삭제만 여기로 올 수 있다
        if (updated == 0) {
            log.error("성인인증 확정 경합 identityVerificationId={} userId={}", identityVerificationId, userId);
            throw ExpiredOrRevokedAdultVerificationException.EXCEPTION;
        }

        return AdultVerificationStatusInfo.verified(verifiedAt, expiresAt);
    }

    @Transactional(readOnly = true)
    public AdultVerificationStatusInfo getStatus(Long userId) {
        LocalDate today = LocalDate.now();
        return adultVerificationAdaptor.findLatestSettled(userId)
                .map(verification -> verification.isActiveAt(today)
                        ? AdultVerificationStatusInfo.verified(verification.getVerifiedAt(), verification.getExpiresAt())
                        : AdultVerificationStatusInfo.expired(verification.getVerifiedAt(), verification.getExpiresAt()))
                .orElseGet(AdultVerificationStatusInfo::notVerified);
    }

    // 정리 후보. 확정이 유실된 건이 섞여 있을 수 있어 배치가 포트원에 하나씩 물어본다
    @Transactional(readOnly = true)
    public List<AdultVerification> findAbandonCandidates(LocalDateTime threshold, int limit) {
        return adultVerificationAdaptor.findAbandonCandidates(threshold, limit);
    }

    @Transactional
    public void abandon(String identityVerificationId) {
        adultVerificationAdaptor.markAbandoned(identityVerificationId);
    }

    // 차단은 읽는 시점에 만료일로 판정되므로 여기서는 상태 정리만 한다
    @Transactional
    public int expireOverdue() {
        return adultVerificationAdaptor.expireOverdue(LocalDate.now());
    }

    // 테스트 전용. 재인증을 반복하려면 이력이 남아 있으면 안 된다
    @Transactional
    public int deleteHistory(Long targetUserId, Long requesterId) {
        int deleted = adultVerificationAdaptor.deleteAllByUserId(targetUserId);

        log.warn(">>> [AdultVerification] 테스트 이력 삭제 targetUserId={} deleted={} requesterId={}",
                targetUserId, deleted, requesterId);
        return deleted;
    }
}
