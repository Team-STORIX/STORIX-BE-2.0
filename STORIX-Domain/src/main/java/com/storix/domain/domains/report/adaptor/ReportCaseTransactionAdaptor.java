package com.storix.domain.domains.report.adaptor;

import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.TargetContentType;
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
     * unique 제약 위반을 별도 트랜잭션에서 즉시 표면화하기 위해 saveAndFlush + REQUIRES_NEW 사용.
     * 실패 시 이 서브트랜잭션만 롤백되어 호출자(부모 트랜잭션)의 영속성 컨텍스트는 오염되지 않고,
     * 호출자는 기존 케이스를 재조회해 재시도할 수 있다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReportCase create(TargetContentType targetType, Long targetId, Long reportedUserId) {
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
