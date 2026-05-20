package com.storix.domain.domains.review.repository;

import com.storix.domain.domains.review.domain.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

    long countByReportCaseId(Long reportCaseId);

    List<ReviewReport> findAllByReportCaseIdOrderByCreatedAtAsc(Long reportCaseId);
}
