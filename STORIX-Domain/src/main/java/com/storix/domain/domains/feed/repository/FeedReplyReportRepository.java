package com.storix.domain.domains.feed.repository;

import com.storix.domain.domains.feed.domain.FeedReplyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedReplyReportRepository extends JpaRepository<FeedReplyReport, Long> {

    @Query("""
            SELECT report.reportCaseId AS reportCaseId, COUNT(report) AS reportCount
            FROM FeedReplyReport report
            WHERE report.reportCaseId IN :reportCaseIds
            GROUP BY report.reportCaseId
            """)
    List<ReportCaseCountProjection> countByReportCaseIds(@Param("reportCaseIds") List<Long> reportCaseIds);

    List<FeedReplyReport> findAllByReportCaseIdOrderByCreatedAtAsc(Long reportCaseId);

    boolean existsByReporterIdAndReplyId(Long reporterId, Long replyId);
}
