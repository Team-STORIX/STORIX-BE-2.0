package com.storix.storix_api.domains.review.repository;

import com.storix.storix_api.domains.review.domain.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
}
