package com.storix.domain.domains.report.repository;

import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.TargetContentType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReportCaseRepository extends JpaRepository<ReportCase, Long>, ReportCaseRepositoryCustom {

    Optional<ReportCase> findByTargetTypeAndTargetIdAndReportedUserId(
            TargetContentType targetType,
            Long targetId,
            Long reportedUserId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReportCase r WHERE r.id = :id")
    Optional<ReportCase> findByIdForUpdate(@Param("id") Long id);

    long countByStatus(ReportStatus status);

    @Query("""
            SELECT r.status AS status, COUNT(r) AS count
            FROM ReportCase r
            WHERE r.reportedUserId = :userId
            GROUP BY r.status
            """)
    List<StatusCountProjection> countGroupByStatusAndReportedUserId(@Param("userId") Long userId);

    // 처리 완료 후 보유기간 경과한 신고 케이스 id 조회
    @Query("SELECT r.id FROM ReportCase r WHERE r.processedAt < :cutoff AND r.status IN :statuses")
    List<Long> findIdsByProcessedAtBeforeAndStatusIn(@Param("cutoff") LocalDateTime cutoff,
                                                      @Param("statuses") List<ReportStatus> statuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ReportCase r WHERE r.id IN :ids")
    int deleteByIdIn(@Param("ids") List<Long> ids);
}
