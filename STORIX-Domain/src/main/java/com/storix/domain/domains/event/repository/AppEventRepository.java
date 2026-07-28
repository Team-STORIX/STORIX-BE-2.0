package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppEventRepository extends JpaRepository<AppEvent, Long> {

    Page<AppEvent> findAllByOrderByIdDesc(Pageable pageable);

    // 이벤트명 검색 — keyword null이면 전체 조회
    @Query("SELECT e FROM AppEvent e WHERE (:keyword IS NULL OR e.name LIKE %:keyword%) ORDER BY e.id DESC")
    Page<AppEvent> searchByName(@Param("keyword") String keyword, Pageable pageable);

    // 지금 진행 중인 해당 타입 이벤트
    // 결과 최대 1건이나, 과거 데이터 방어를 위해 최신순 List로 설정함
    @Query("""
            SELECT e FROM AppEvent e
            WHERE e.eventType = :eventType
              AND e.startAt <= :now
              AND e.endAt > :now
            ORDER BY e.startAt DESC
            """)
    List<AppEvent> findActiveByType(@Param("eventType") AppEventType eventType,
                                    @Param("now") LocalDateTime now);

    // 진행 중인 이벤트가 없을 때 기간 정보를 내려주기 위한 폴백
    Optional<AppEvent> findFirstByEventTypeOrderByStartAtDesc(AppEventType eventType);

    // 같은 타입 이벤트끼리 기간이 겹치는지. 수정 시에는 자기 자신을 제외한다
    @Query("""
            SELECT COUNT(e) > 0 FROM AppEvent e
            WHERE e.eventType = :eventType
              AND (:excludeId IS NULL OR e.id <> :excludeId)
              AND e.startAt < :endAt
              AND e.endAt > :startAt
            """)
    boolean existsOverlappingByType(@Param("eventType") AppEventType eventType,
                                    @Param("startAt") LocalDateTime startAt,
                                    @Param("endAt") LocalDateTime endAt,
                                    @Param("excludeId") Long excludeId);
}
