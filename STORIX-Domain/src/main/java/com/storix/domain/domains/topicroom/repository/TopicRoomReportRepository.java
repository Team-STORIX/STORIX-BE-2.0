package com.storix.domain.domains.topicroom.repository;

import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRoomReportRepository extends JpaRepository<TopicRoomReport, Long> {

    long countByReportCaseId(Long reportCaseId);

    List<TopicRoomReport> findAllByReportCaseIdOrderByCreatedAtAsc(Long reportCaseId);
}
