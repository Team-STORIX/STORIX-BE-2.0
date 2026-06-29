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

    @Query("""
            SELECT new com.storix.domain.domains.user.dto.AdminUserReportItemResponse(
                report.id,
                com.storix.domain.domains.user.dto.AdminUserReportDirection.WRITTEN,
                report.reportCaseId,
                com.storix.domain.domains.report.domain.ReportTargetType.FEED,
                report.boardId,
                report.reporterId,
                report.reportedUserId,
                null,
                null,
                reportCase.status,
                reportCase.processAction,
                report.createdAt
            )
            FROM FeedReport report
            LEFT JOIN ReportCase reportCase ON report.reportCaseId = reportCase.id
            WHERE report.reporterId = :userId
            ORDER BY report.createdAt DESC, report.id DESC
            """)
    List<com.storix.domain.domains.user.dto.AdminUserReportItemResponse> findAdminReportsByReporterId(@Param("userId") Long userId);

    @Query("""
            SELECT new com.storix.domain.domains.user.dto.AdminUserReportItemResponse(
                report.id,
                com.storix.domain.domains.user.dto.AdminUserReportDirection.RECEIVED,
                report.reportCaseId,
                com.storix.domain.domains.report.domain.ReportTargetType.FEED,
                report.boardId,
                report.reporterId,
                report.reportedUserId,
                null,
                null,
                reportCase.status,
                reportCase.processAction,
                report.createdAt
            )
            FROM FeedReport report
            LEFT JOIN ReportCase reportCase ON report.reportCaseId = reportCase.id
            WHERE report.reportedUserId = :userId
            ORDER BY report.createdAt DESC, report.id DESC
            """)
    List<com.storix.domain.domains.user.dto.AdminUserReportItemResponse> findAdminReportsByReportedUserId(@Param("userId") Long userId);

    boolean existsByReporterIdAndBoardId(Long reporterId, Long boardId);

    long countByReporterId(Long reporterId);

    long countByReportedUserId(Long reportedUserId);
}
