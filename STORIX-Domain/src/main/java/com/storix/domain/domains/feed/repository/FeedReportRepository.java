package com.storix.domain.domains.feed.repository;

import com.storix.domain.domains.feed.domain.FeedReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedReportRepository extends JpaRepository<FeedReport, Long> {
}
