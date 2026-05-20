package com.storix.domain.domains.feed.repository;

import com.storix.domain.domains.feed.domain.FeedReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedReportRepository extends JpaRepository<FeedReport, Long> {

    long countByReportCaseId(Long reportCaseId);

    List<FeedReport> findAllByReportCaseIdOrderByCreatedAtAsc(Long reportCaseId);
}
