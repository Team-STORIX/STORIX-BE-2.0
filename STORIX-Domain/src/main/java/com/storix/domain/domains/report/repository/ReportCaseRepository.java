package com.storix.domain.domains.report.repository;

import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportCaseRepository extends JpaRepository<ReportCase, Long>, ReportCaseRepositoryCustom {

    Optional<ReportCase> findByTargetTypeAndTargetIdAndReportedUserId(
            ReportTargetType targetType,
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
}
