package com.storix.domain.domains.review.repository;

import com.storix.domain.domains.review.domain.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
}
