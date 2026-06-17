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

    boolean existsByReporterIdAndReportedUserIdAndTopicRoomId(Long reporterId, Long reportedUserId, Long topicRoomId);
}
