package com.storix.domain.domains.report.repository;

import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportCaseRepository extends JpaRepository<ReportCase, Long> {

    Optional<ReportCase> findByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);
}
