package com.storix.domain.domains.feed.repository;

import com.storix.domain.domains.feed.domain.FeedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedReportRepository extends JpaRepository<FeedReport, Long> {

    @Query("""
            SELECT report.reportCaseId AS reportCaseId, COUNT(report) AS reportCount
            FROM FeedReport report
            WHERE report.reportCaseId IN :reportCaseIds
            GROUP BY report.reportCaseId
            """)
    List<ReportCaseCountProjection> countByReportCaseIds(@Param("reportCaseIds") List<Long> reportCaseIds);

    List<FeedReport> findAllByReportCaseIdOrderByCreatedAtAsc(Long reportCaseId);
}
