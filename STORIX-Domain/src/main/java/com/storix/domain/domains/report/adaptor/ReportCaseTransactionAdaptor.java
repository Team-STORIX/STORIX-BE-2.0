package com.storix.domain.domains.report.adaptor;

import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import com.storix.domain.domains.report.repository.ReportCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReportCaseTransactionAdaptor {

    private final ReportCaseRepository reportCaseRepository;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Optional<ReportCase> findByTarget(ReportTargetType targetType, Long targetId) {
        return reportCaseRepository.findByTargetTypeAndTargetId(targetType, targetId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReportCase create(ReportTargetType targetType, Long targetId, Long reportedUserId) {
        return reportCaseRepository.saveAndFlush(
                ReportCase.builder()
                        .targetType(targetType)
                        .targetId(targetId)
                        .reportedUserId(reportedUserId)
                        .status(ReportStatus.RECEIVED)
                        .build()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReportCase reopen(ReportCase reportCase) {
        reportCase.reopen();
        return reportCaseRepository.saveAndFlush(reportCase);
    }
}
