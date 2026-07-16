package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.Popup;
import com.storix.domain.domains.event.domain.PopupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PopupRepository extends JpaRepository<Popup, Long> {

    @EntityGraph(attributePaths = "appEvent")
    Page<Popup> findAllByOrderByIdDesc(Pageable pageable);

    // 스케줄러: 노출 시작이 지난 예약 팝업
    List<Popup> findAllByStatusAndDisplayStartAtLessThanEqual(PopupStatus status, LocalDateTime now);

    // 스케줄러: 노출 종료가 지난 활성 팝업
    List<Popup> findAllByStatusAndDisplayEndAtLessThan(PopupStatus status, LocalDateTime now);

    // AppEvent 강제 종료 시 cascade 대상
    List<Popup> findAllByAppEvent_IdAndStatusNot(Long appEventId, PopupStatus status);

    // 앱 홈 표시용 활성 팝업
    @Query("""
        SELECT p
        FROM Popup p
        WHERE p.status = :status
          AND p.displayStartAt <= :now
          AND p.displayEndAt >= :now
        ORDER BY p.displayStartAt DESC, p.id DESC
    """)
    Optional<Popup> findActivePopup(
            @Param("status") PopupStatus status,
            @Param("now") LocalDateTime now
    );

    // 등록/수정 시 기간 중복 팝업 검증
    @Query("""
        SELECT (COUNT(p) > 0)
        FROM Popup p
        WHERE p.status <> com.storix.domain.domains.event.domain.PopupStatus.ENDED
          AND (:excludeId IS NULL OR p.id <> :excludeId)
          AND p.displayStartAt <= :displayEndAt
          AND p.displayEndAt >= :displayStartAt
    """)
    boolean existsOverlappingActivePopup(
            @Param("displayStartAt") LocalDateTime displayStartAt,
            @Param("displayEndAt") LocalDateTime displayEndAt,
            @Param("excludeId") Long excludeId
    );
}
