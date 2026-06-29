package com.storix.domain.domains.topicroom.repository;

import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicRoomReportRepository extends JpaRepository<TopicRoomReport, Long> {

    @Query("""
            SELECT report.reportCaseId AS reportCaseId, COUNT(report) AS reportCount
            FROM TopicRoomReport report
            WHERE report.reportCaseId IN :reportCaseIds
            GROUP BY report.reportCaseId
            """)
    List<ReportCaseCountProjection> countByReportCaseIds(@Param("reportCaseIds") List<Long> reportCaseIds);

    List<TopicRoomReport> findAllByReportCaseIdOrderByCreatedAtAsc(Long reportCaseId);

    @Query("""
            SELECT new com.storix.domain.domains.user.dto.AdminUserReportItemResponse(
                report.id,
                com.storix.domain.domains.user.dto.AdminUserReportDirection.WRITTEN,
                report.reportCaseId,
                com.storix.domain.domains.report.domain.ReportTargetType.TOPIC_ROOM,
                report.topicRoomId,
                report.reporterId,
                report.reportedUserId,
                report.reason,
                report.otherReason,
                reportCase.status,
                reportCase.processAction,
                report.createdAt
            )
            FROM TopicRoomReport report
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
                com.storix.domain.domains.report.domain.ReportTargetType.TOPIC_ROOM,
                report.topicRoomId,
                report.reporterId,
                report.reportedUserId,
                report.reason,
                report.otherReason,
                reportCase.status,
                reportCase.processAction,
                report.createdAt
            )
            FROM TopicRoomReport report
            LEFT JOIN ReportCase reportCase ON report.reportCaseId = reportCase.id
            WHERE report.reportedUserId = :userId
            ORDER BY report.createdAt DESC, report.id DESC
            """)
    List<com.storix.domain.domains.user.dto.AdminUserReportItemResponse> findAdminReportsByReportedUserId(@Param("userId") Long userId);

    boolean existsByReporterIdAndReportedUserIdAndTopicRoomId(Long reporterId, Long reportedUserId, Long topicRoomId);

    long countByReporterId(Long reporterId);

    long countByReportedUserId(Long reportedUserId);
}
