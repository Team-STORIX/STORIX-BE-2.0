package com.storix.api.domain.report.controller.dto;

import com.storix.domain.domains.report.domain.ReportAction;
import com.storix.domain.domains.report.domain.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminReportProcessRequest(
        @NotNull ReportStatus status,
        ReportAction processAction,
        @Size(max = 500) String processMemo
) {}
