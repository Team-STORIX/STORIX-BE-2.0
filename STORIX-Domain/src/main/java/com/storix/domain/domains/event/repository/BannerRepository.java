package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.Banner;
import com.storix.domain.domains.event.domain.BannerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    @EntityGraph(attributePaths = "appEvent")
    Page<Banner> findAllByOrderByIdDesc(Pageable pageable);

    // 스케줄러: 노출 시작이 지난 예약 배너
    List<Banner> findAllByStatusAndDisplayStartAtLessThanEqual(BannerStatus status, LocalDateTime now);

    // 스케줄러: 노출 종료가 지난 활성 배너
    List<Banner> findAllByStatusAndDisplayEndAtLessThan(BannerStatus status, LocalDateTime now);

    // AppEvent 강제 종료 시 cascade 대상
    List<Banner> findAllByAppEvent_IdAndStatusNot(Long appEventId, BannerStatus status);

    // 앱 홈 표시용 활성 배너
    @Query("""
        SELECT b
        FROM Banner b
        WHERE b.status = :status
          AND b.displayStartAt <= :now
          AND b.displayEndAt >= :now
        ORDER BY b.displayStartAt DESC, b.id DESC
    """)
    Optional<Banner> findActiveBanner(
            @Param("status") BannerStatus status,
            @Param("now") LocalDateTime now
    );

    // 등록/수정 시 기간 중복 배너 검증
    @Query("""
        SELECT (COUNT(b) > 0)
        FROM Banner b
        WHERE b.status <> com.storix.domain.domains.event.domain.BannerStatus.ENDED
          AND (:excludeId IS NULL OR b.id <> :excludeId)
          AND b.displayStartAt <= :displayEndAt
          AND b.displayEndAt >= :displayStartAt
    """)
    boolean existsOverlappingActiveBanner(
            @Param("displayStartAt") LocalDateTime displayStartAt,
            @Param("displayEndAt") LocalDateTime displayEndAt,
            @Param("excludeId") Long excludeId
    );
}
