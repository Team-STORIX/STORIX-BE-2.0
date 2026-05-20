package com.storix.domain.domains.report.adaptor;

import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import com.storix.domain.domains.report.repository.ReportCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportCaseAdaptor {

    private final ReportCaseRepository reportCaseRepository;

    public ReportCase findOrCreate(ReportTargetType targetType, Long targetId) {
        return reportCaseRepository.findByTargetTypeAndTargetId(targetType, targetId)
                .orElseGet(() -> reportCaseRepository.save(
                        ReportCase.builder()
                                .targetType(targetType)
                                .targetId(targetId)
                                .status(ReportStatus.RECEIVED)
                                .build()
                ));
    }
}
