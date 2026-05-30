package com.storix.domain.domains.report.repository;

import com.storix.domain.domains.report.domain.ReportAction;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReportCaseRepository extends JpaRepository<ReportCase, Long>, ReportCaseRepositoryCustom {

    Optional<ReportCase> findByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);

    long countByStatus(ReportStatus status);

    long countByReportedUserId(Long reportedUserId);

    long countByReportedUserIdAndStatus(Long reportedUserId, ReportStatus status);

    List<ReportCase> findByStatusAndProcessActionAndProcessedAtBefore(
            ReportStatus status, ReportAction processAction, LocalDateTime threshold);

    boolean existsByReportedUserIdAndStatusAndProcessActionAndProcessedAtAfter(
            Long reportedUserId, ReportStatus status, ReportAction processAction, LocalDateTime threshold);
}
