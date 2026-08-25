package com.storix.domain.domains.adultverification.repository;

import com.storix.domain.domains.adultverification.domain.AdultVerification;
import com.storix.domain.domains.adultverification.domain.AdultVerificationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdultVerificationRepository extends JpaRepository<AdultVerification, Long> {

    Optional<AdultVerification> findByIdentityVerificationId(String identityVerificationId);

    // 성인 콘텐츠 접근을 막을지 판단할 때 쓴다. 유효기간은 읽는 쪽에서 날짜로 따진다
    @Query("SELECT MAX(av.verifiedAt) FROM AdultVerification av WHERE av.userId = :userId AND av.status = :verified")
    LocalDateTime findLatestVerifiedAtByUserId(@Param("userId") Long userId,
                                               @Param("verified") AdultVerificationStatus verified);

    // 아직 확정을 기다리는 티켓. 새로 만들지 않고 이걸 다시 내준다
    Optional<AdultVerification> findFirstByUserIdAndStatusOrderByIdDesc(Long userId, AdultVerificationStatus status);

    // 아직 확정되지 않은 최신 건. 방치로 정리된 것도 포함해 포트원 상태를 확인한다
    Optional<AdultVerification> findFirstByUserIdAndStatusInOrderByIdDesc(
            Long userId, List<AdultVerificationStatus> statuses);

    // 만료된 건도 같이 본다. 그래야 한 번도 안 한 것과 만료된 것을 구분한다
    Optional<AdultVerification> findFirstByUserIdAndStatusInOrderByVerifiedAtDesc(
            Long userId, List<AdultVerificationStatus> statuses);

    // 동시에 들어와도 한 요청만 성공한다. 바뀐 행이 0이면 다른 요청이 이미 처리한 것
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdultVerification av
        SET av.status = :verified,
            av.verifiedAt = :verifiedAt,
            av.expiresAt = :expiresAt,
            av.providerTransactionId = :providerTransactionId,
            av.updatedAt = :now
        WHERE av.identityVerificationId = :identityVerificationId
          AND av.status IN :confirmable
    """)
    int markVerifiedIfRetryable(@Param("identityVerificationId") String identityVerificationId,
                                @Param("verified") AdultVerificationStatus verified,
                                @Param("confirmable") List<AdultVerificationStatus> confirmable,
                                @Param("verifiedAt") LocalDateTime verifiedAt,
                                @Param("expiresAt") LocalDate expiresAt,
                                @Param("providerTransactionId") String providerTransactionId,
                                @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdultVerification av
        SET av.status = :expired,
            av.updatedAt = :now
        WHERE av.status = :verified
          AND av.expiresAt < :today
    """)
    int expireOverdue(@Param("expired") AdultVerificationStatus expired,
                      @Param("verified") AdultVerificationStatus verified,
                      @Param("today") LocalDate today,
                      @Param("now") LocalDateTime now);

    // 티켓을 다시 내줄 때마다 방치로 볼 기준 시각을 미룬다
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdultVerification av
        SET av.updatedAt = :now
        WHERE av.identityVerificationId = :identityVerificationId
          AND av.status = :pending
    """)
    int markReissued(@Param("identityVerificationId") String identityVerificationId,
                     @Param("pending") AdultVerificationStatus pending,
                     @Param("now") LocalDateTime now);

    // 정리 후보. 포트원에 하나씩 물어봐야 해서 목록으로 가져온다
    List<AdultVerification> findByStatusAndUpdatedAtBefore(
            AdultVerificationStatus status, LocalDateTime threshold, Pageable pageable);

    // 포트원에서도 끝나지 않은 건으로 확인된 티켓을 닫는다
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdultVerification av
        SET av.status = :abandoned,
            av.updatedAt = :now
        WHERE av.identityVerificationId = :identityVerificationId
          AND av.status = :pending
    """)
    int markAbandoned(@Param("identityVerificationId") String identityVerificationId,
                      @Param("abandoned") AdultVerificationStatus abandoned,
                      @Param("pending") AdultVerificationStatus pending,
                      @Param("now") LocalDateTime now);

    // 유저 탈퇴 시 인증 이력 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AdultVerification av WHERE av.userId = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
