package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.PopupDismiss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PopupDismissRepository extends JpaRepository<PopupDismiss, Long> {

    boolean existsByUserIdAndPopup_IdAndDismissedOn(Long userId, Long popupId, LocalDate dismissedOn);

    // (userId, popupId) 유니크 기반 원자적 upsert, 동시 요청도 안전
    @Modifying
    @Query(value = """
            INSERT INTO event_popup_dismisses (user_id, popup_id, dismissed_on, created_at, updated_at)
            VALUES (:userId, :popupId, :today, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                dismissed_on = :today,
                updated_at = NOW()
            """, nativeQuery = true)
    void upsertDismiss(@Param("userId") Long userId,
                       @Param("popupId") Long popupId,
                       @Param("today") LocalDate today);
}
