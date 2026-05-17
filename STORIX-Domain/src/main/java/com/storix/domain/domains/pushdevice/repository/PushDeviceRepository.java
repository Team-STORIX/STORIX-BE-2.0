package com.storix.domain.domains.pushdevice.repository;

import com.storix.domain.domains.pushdevice.domain.PushDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PushDeviceRepository extends JpaRepository<PushDevice, Long> {

    // 단일 디바이스 조회
    Optional<PushDevice> findByUserIdAndInstallationId(Long userId, String installationId);

    // 한 유저의 활성 디바이스 FCM 토큰 일괄 조회
    @Query("SELECT d.fcmToken FROM PushDevice d WHERE d.userId = :userId AND d.isActive = true")
    List<String> findActiveFcmTokensByUserId(@Param("userId") Long userId);

    // 단일 디바이스 비활성화
    @Modifying(clearAutomatically = true)
    @Query("UPDATE PushDevice d SET d.isActive = false WHERE d.userId = :userId AND d.installationId = :installationId")
    int deactivateByUserIdAndInstallationId(@Param("userId") Long userId,
                                            @Param("installationId") String installationId);

    // FCM invalid 토큰 일괄 비활성화
    @Modifying(clearAutomatically = true)
    @Query("UPDATE PushDevice d SET d.isActive = false WHERE d.fcmToken IN :tokens")
    int deactivateByFcmTokens(@Param("tokens") List<String> tokens);

    // 유저 탈퇴 시 해당 유저의 모든 활성 디바이스 일괄 비활성화
    @Modifying(clearAutomatically = true)
    @Query("UPDATE PushDevice d SET d.isActive = false WHERE d.userId = :userId AND d.isActive = true")
    int deactivateAllByUserId(@Param("userId") Long userId);

    // 발송 성공한 토큰들의 lastSuccessAt 갱신
    @Modifying(clearAutomatically = true)
    @Query("UPDATE PushDevice d SET d.lastSuccessAt = :now WHERE d.fcmToken IN :tokens AND d.isActive = true")
    void markFcmTokensSuccess(@Param("tokens") List<String> tokens, @Param("now") LocalDateTime now);
}
