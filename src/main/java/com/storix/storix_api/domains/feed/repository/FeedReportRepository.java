package com.storix.storix_api.domains.feed.repository;

import com.storix.storix_api.domains.feed.domain.FeedReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedReportRepository extends JpaRepository<FeedReport, Long> {
}
