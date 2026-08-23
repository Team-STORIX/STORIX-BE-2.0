package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.AppEventPageModalConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppEventPageModalConfirmationRepository extends JpaRepository<AppEventPageModalConfirmation, Long> {

    boolean existsByUserIdAndAppEventId(Long userId, Long appEventId);

    // (userId, appEventId) 유니크 기반 원자적 upsert. IGNORE와 달리 중복 키 외의 오류는 그대로 던진다
    @Modifying
    @Query(value = """
            INSERT INTO event_page_modal_confirmations (user_id, app_event_id, created_at, updated_at)
            VALUES (:userId, :appEventId, NOW(), NOW())
            ON DUPLICATE KEY UPDATE updated_at = updated_at
            """, nativeQuery = true)
    void insertIfAbsent(@Param("userId") Long userId, @Param("appEventId") Long appEventId);
}
