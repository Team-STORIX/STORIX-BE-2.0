package com.storix.domain.domains.report.adaptor;

import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import com.storix.domain.domains.report.repository.StatusCountProjection;
import com.storix.domain.domains.report.dto.AdminReportSearchCondition;
import com.storix.domain.domains.report.exception.UnknownReportCaseException;
import com.storix.domain.domains.report.repository.ReportCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

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

    // report 저장 성공 후 같은 트랜잭션 안에서 호출 — reopen은 부모 트랜잭션과 함께 커밋/롤백
    public void reopenIfClosed(ReportCase reportCase) {
        if (reportCase.getStatus() != ReportStatus.RECEIVED) {
            reportCase.reopen();
        }
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

    public Map<ReportStatus, Long> countGroupByStatus(Long reportedUserId) {
        return reportCaseRepository.countGroupByStatusAndReportedUserId(reportedUserId).stream()
                .collect(Collectors.toMap(
                        StatusCountProjection::getStatus,
                        StatusCountProjection::getCount
                ));
    }
}
