package com.storix.domain.domains.report.adaptor;

import com.storix.domain.domains.report.domain.ReportAction;
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

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportCaseAdaptor {

    private final ReportCaseRepository reportCaseRepository;
    private final ReportCaseTransactionAdaptor reportCaseTransactionAdaptor;

    public ReportCase findOrCreate(ReportTargetType targetType, Long targetId, Long reportedUserId) {
        return reportCaseTransactionAdaptor.findByTarget(targetType, targetId)
                .orElseGet(() -> {
                    try {
                        return reportCaseTransactionAdaptor.create(targetType, targetId, reportedUserId);
                    } catch (DataIntegrityViolationException e) {
                        return reportCaseTransactionAdaptor.findByTarget(targetType, targetId)
                                .orElseThrow(() -> e);
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

    public long countByReportedUserId(Long reportedUserId) {
        return reportCaseRepository.countByReportedUserId(reportedUserId);
    }

    public long countByReportedUserIdAndStatus(Long reportedUserId, ReportStatus status) {
        return reportCaseRepository.countByReportedUserIdAndStatus(reportedUserId, status);
    }

    public List<ReportCase> findExpiredSuspensions(LocalDateTime threshold) {
        return reportCaseRepository.findByStatusAndProcessActionAndProcessedAtBefore(
                ReportStatus.COMPLETED, ReportAction.ACCOUNT_SUSPENDED, threshold);
    }
}
