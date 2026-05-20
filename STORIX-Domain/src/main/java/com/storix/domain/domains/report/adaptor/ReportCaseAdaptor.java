package com.storix.domain.domains.report.adaptor;

import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import com.storix.domain.domains.report.dto.AdminReportSearchCondition;
import com.storix.domain.domains.report.exception.UnknownReportCaseException;
import com.storix.domain.domains.report.repository.ReportCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportCaseAdaptor {

    private final ReportCaseRepository reportCaseRepository;

    public ReportCase findOrCreate(ReportTargetType targetType, Long targetId, Long reportedUserId) {
        return reportCaseRepository.findByTargetTypeAndTargetId(targetType, targetId)
                .orElseGet(() -> {
                    try {
                        return reportCaseRepository.save(
                                ReportCase.builder()
                                        .targetType(targetType)
                                        .targetId(targetId)
                                        .reportedUserId(reportedUserId)
                                        .status(ReportStatus.RECEIVED)
                                        .build()
                        );
                    } catch (DataIntegrityViolationException e) {
                        return reportCaseRepository.findByTargetTypeAndTargetId(targetType, targetId)
                                .orElseThrow();
                    }
                });
    }

    public ReportCase findById(Long reportCaseId) {
        return reportCaseRepository.findById(reportCaseId)
                .orElseThrow(() -> UnknownReportCaseException.EXCEPTION);
    }

    public Page<ReportCase> searchReportCases(AdminReportSearchCondition condition, Pageable pageable) {
        return reportCaseRepository.searchReportCases(condition, pageable);
    }

    public long countByStatus(ReportStatus status) {
        return reportCaseRepository.countByStatus(status);
    }
}
