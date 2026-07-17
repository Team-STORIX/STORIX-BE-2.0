package com.storix.domain.domains.topicroom.repository;

import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.report.repository.ReportedUserCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    boolean existsByReporterIdAndReportedUserIdAndTopicRoomIdAndChatMessageId(Long reporterId, Long reportedUserId, Long topicRoomId, Long chatMessageId);

    long countByReporterId(Long reporterId);

    long countByReportedUserId(Long reportedUserId);

    @Query("""
            SELECT report.reportedUserId AS reportedUserId, COUNT(report) AS reportCount
            FROM TopicRoomReport report
            WHERE report.reportedUserId IN :reportedUserIds
            GROUP BY report.reportedUserId
            """)
    List<ReportedUserCountProjection> countByReportedUserIds(@Param("reportedUserIds") List<Long> reportedUserIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TopicRoomReport report WHERE report.reportCaseId IN :reportCaseIds")
    int deleteByReportCaseIdIn(@Param("reportCaseIds") List<Long> reportCaseIds);
}
