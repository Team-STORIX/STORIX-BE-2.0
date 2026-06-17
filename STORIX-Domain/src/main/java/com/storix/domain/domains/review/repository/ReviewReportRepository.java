package com.storix.domain.domains.review.repository;

import com.storix.domain.domains.review.domain.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

    @Query("""
            SELECT report.reportCaseId AS reportCaseId, COUNT(report) AS reportCount
            FROM ReviewReport report
            WHERE report.reportCaseId IN :reportCaseIds
            GROUP BY report.reportCaseId
            """)
    List<ReportCaseCountProjection> countByReportCaseIds(@Param("reportCaseIds") List<Long> reportCaseIds);

    List<ReviewReport> findAllByReportCaseIdOrderByCreatedAtAsc(Long reportCaseId);

    boolean existsByReporterIdAndReviewId(Long reporterId, Long reviewId);
}
