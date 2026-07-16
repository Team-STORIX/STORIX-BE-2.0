package com.storix.domain.domains.report.repository;

import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.dto.AdminReportSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportCaseRepositoryCustom {

    Page<ReportCase> searchReportCases(AdminReportSearchCondition condition, Pageable pageable);
}
