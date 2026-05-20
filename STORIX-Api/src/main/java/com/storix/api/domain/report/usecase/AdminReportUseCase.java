package com.storix.api.domain.report.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import com.storix.domain.domains.report.dto.AdminReportDetailResponse;
import com.storix.domain.domains.report.dto.AdminReportListResponse;
import com.storix.domain.domains.report.dto.AdminReportSearchCondition;
import com.storix.domain.domains.report.service.AdminReportQueryService;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.Role;
import com.storix.domain.domains.user.exception.auth.ForbiddenApproachException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@UseCase
@RequiredArgsConstructor
public class AdminReportUseCase {

    private final AdminReportQueryService adminReportQueryService;

    public CustomResponse<Page<AdminReportListResponse>> getReports(
            AuthUserDetails authUserDetails,
            ReportTargetType targetType,
            ReportStatus status,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Pageable pageable
    ) {
        validateAdmin(authUserDetails);

        Page<AdminReportListResponse> result = adminReportQueryService.getReports(
                new AdminReportSearchCondition(targetType, status, startAt, endAt),
                pageable
        );
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, result);
    }

    public CustomResponse<Long> getUnprocessedCount(AuthUserDetails authUserDetails) {
        validateAdmin(authUserDetails);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS, adminReportQueryService.countUnprocessedReports());
    }

    public CustomResponse<AdminReportDetailResponse> getReportDetail(AuthUserDetails authUserDetails, Long reportCaseId) {
        validateAdmin(authUserDetails);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS, adminReportQueryService.getReportDetail(reportCaseId));
    }

    private void validateAdmin(AuthUserDetails authUserDetails) {
        if (authUserDetails == null || authUserDetails.getRole() != Role.ADMIN) {
            throw ForbiddenApproachException.EXCEPTION;
        }
    }
}
