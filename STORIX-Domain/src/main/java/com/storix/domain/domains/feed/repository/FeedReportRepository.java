package com.storix.domain.domains.feed.repository;

import com.storix.domain.domains.feed.domain.FeedReport;
import com.storix.domain.domains.report.repository.ReportedUserCountProjection;
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

    boolean existsByReporterIdAndBoardId(Long reporterId, Long boardId);

    long countByReporterId(Long reporterId);

    long countByReportedUserId(Long reportedUserId);

    @Query("""
            SELECT report.reportedUserId AS reportedUserId, COUNT(report) AS reportCount
            FROM FeedReport report
            WHERE report.reportedUserId IN :reportedUserIds
            GROUP BY report.reportedUserId
            """)
    List<ReportedUserCountProjection> countByReportedUserIds(@Param("reportedUserIds") List<Long> reportedUserIds);
}
