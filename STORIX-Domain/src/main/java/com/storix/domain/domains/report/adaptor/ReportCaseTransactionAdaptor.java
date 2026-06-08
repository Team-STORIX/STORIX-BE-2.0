package com.storix.domain.domains.report.adaptor;

import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import com.storix.domain.domains.report.repository.ReportCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReportCaseTransactionAdaptor {

    private final ReportCaseRepository reportCaseRepository;

    /**
     * 기존 케이스를 찾아 reopen까지 처리한 뒤 반환.
     * REQUIRES_NEW 내에서 reopen()을 호출하므로 dirty checking이 정상 동작한다.
     * 케이스가 없으면 null 반환 (create를 호출해야 함).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReportCase findByTargetAndReopen(ReportTargetType targetType, Long targetId) {
        return reportCaseRepository.findByTargetTypeAndTargetId(targetType, targetId)
                .map(rc -> {
                    if (rc.getStatus() != ReportStatus.RECEIVED) {
                        rc.reopen();
                    }
                    return rc;
                })
                .orElse(null);
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

}
