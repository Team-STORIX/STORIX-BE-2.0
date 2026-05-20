package com.storix.domain.domains.feed.repository;

import com.storix.domain.domains.feed.domain.FeedReplyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedReplyReportRepository extends JpaRepository<FeedReplyReport, Long> {

    long countByReportCaseId(Long reportCaseId);

    List<FeedReplyReport> findAllByReportCaseIdOrderByCreatedAtAsc(Long reportCaseId);
}
