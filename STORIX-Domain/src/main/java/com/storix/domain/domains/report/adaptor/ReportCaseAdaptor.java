package com.storix.domain.domains.report.adaptor;

import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.TargetContentType;
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

    public ReportCase findOrCreate(TargetContentType targetType, Long targetId, Long reportedUserId) {
        ReportCase existing = findAndReopenIfClosed(targetType, targetId, reportedUserId);
        if (existing != null) return existing;
        try {
            return reportCaseTransactionAdaptor.create(targetType, targetId, reportedUserId);
        } catch (DataIntegrityViolationException e) {
            ReportCase retried = findAndReopenIfClosed(targetType, targetId, reportedUserId);
            if (retried == null) throw e;
            return retried;
        }
    }

    /**
     * 호출자(신고 저장 흐름)의 트랜잭션에 그대로 참여한다.
     * SELECT + reopen()의 dirty checking만 발생하므로 별도 트랜잭션이 필요 없고,
     * 같은 트랜잭션에서 묶여야 신고 저장 실패 시 reopen도 함께 롤백되어 원자성이 보장된다.
     */
    private ReportCase findAndReopenIfClosed(TargetContentType targetType, Long targetId, Long reportedUserId) {
        return reportCaseRepository.findByTargetTypeAndTargetIdAndReportedUserId(targetType, targetId, reportedUserId)
                .map(rc -> {
                    if (rc.getStatus() != ReportStatus.RECEIVED) {
                        rc.reopen();
                    }
                    return rc;
                })
                .orElse(null);
    }

    public ReportCase findById(Long reportCaseId) {
        return reportCaseRepository.findById(reportCaseId)
                .orElseThrow(() -> UnknownReportCaseException.EXCEPTION);
    }

    public ReportCase findByIdForUpdate(Long reportCaseId) {
        return reportCaseRepository.findByIdForUpdate(reportCaseId)
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
