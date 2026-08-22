package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.BannerModalConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BannerModalConfirmationRepository extends JpaRepository<BannerModalConfirmation, Long> {

    boolean existsByUserIdAndBanner_Id(Long userId, Long bannerId);

    // (userId, bannerId) 유니크 기반 원자적 upsert
    @Modifying
    @Query(value = """
            INSERT INTO event_banner_modal_confirmations (user_id, banner_id, created_at, updated_at)
            VALUES (:userId, :bannerId, NOW(), NOW())
            ON DUPLICATE KEY UPDATE updated_at = updated_at
            """, nativeQuery = true)
    void insertIfAbsent(@Param("userId") Long userId, @Param("bannerId") Long bannerId);
}
