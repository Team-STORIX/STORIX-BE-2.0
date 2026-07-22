package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.PopupDismiss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PopupDismissRepository extends JpaRepository<PopupDismiss, Long> {

    boolean existsByUserIdAndPopup_IdAndPermanentTrue(Long userId, Long popupId);

    // 오늘 '다시 안 보기' 또는 '다시 보지 않기'(영구) 여부
    @Query("""
            select count(d) > 0 from PopupDismiss d
            where d.userId = :userId and d.popup.id = :popupId
              and (d.permanent = true or d.dismissedOn = :today)
            """)
    boolean existsSuppressedOn(@Param("userId") Long userId,
                               @Param("popupId") Long popupId,
                               @Param("today") LocalDate today);

    // (userId, popupId) 유니크 기반 원자적 upsert, 동시 요청도 안전. 영구 dismiss는 daily 요청으로 되돌리지 않는다
    @Modifying
    @Query(value = """
            INSERT INTO event_popup_dismisses (user_id, popup_id, dismissed_on, is_permanent, created_at, updated_at)
            VALUES (:userId, :popupId, :today, :permanent, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                dismissed_on = :today,
                is_permanent = (is_permanent OR :permanent),
                updated_at = NOW()
            """, nativeQuery = true)
    void upsertDismiss(@Param("userId") Long userId,
                       @Param("popupId") Long popupId,
                       @Param("today") LocalDate today,
                       @Param("permanent") boolean permanent);
}
