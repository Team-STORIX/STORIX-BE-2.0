package com.storix.api.domain.report.controller.dto;

import com.storix.domain.domains.report.domain.ReportAction;
import com.storix.domain.domains.report.domain.ReportStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminReportProcessRequest(
        @NotNull ReportStatus status,
        List<ReportAction> processActions,
        @Size(max = 500) String processMemo
) {
    @AssertTrue(message = "처리 상태는 REJECTED 또는 COMPLETED만 허용됩니다")
    private boolean isValidStatus() {
        return status == ReportStatus.REJECTED || status == ReportStatus.COMPLETED;
    }

    @AssertTrue(message = "COMPLETED 처리 시 processActions는 필수입니다")
    private boolean isActionRequiredWhenCompleted() {
        return status != ReportStatus.COMPLETED || (processActions != null && !processActions.isEmpty());
    }

    @AssertTrue(message = "REJECTED 처리 시 processActions는 허용되지 않습니다")
    private boolean isNoActionWhenRejected() {
        return status != ReportStatus.REJECTED || processActions == null || processActions.isEmpty();
    }
}
