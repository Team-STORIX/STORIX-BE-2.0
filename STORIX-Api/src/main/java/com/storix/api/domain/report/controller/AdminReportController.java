package com.storix.api.domain.report.controller;

import com.storix.api.domain.report.controller.dto.AdminReportProcessRequest;
import com.storix.api.domain.report.usecase.AdminReportUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.TargetContentType;
import com.storix.domain.domains.report.dto.AdminReportDetailResponse;
import com.storix.domain.domains.report.dto.AdminReportListResponse;
import com.storix.domain.domains.report.dto.AdminUserReportSummaryResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@Tag(name = "관리자 신고", description = "관리자 신고 관리 API")
public class AdminReportController {

    private final AdminReportUseCase adminReportUseCase;

    @GetMapping
    @Operation(summary = "관리자 신고 목록 조회", description = "신고 유형, 처리 상태, 접수 기간, 피신고자 ID로 신고 케이스 목록을 조회합니다.")
    public CustomResponse<Page<AdminReportListResponse>> getReports(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam(required = false) TargetContentType targetType,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) Long reportedUserId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return adminReportUseCase.getReports(authUserDetails, targetType, status, startAt, endAt, reportedUserId, pageable);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "유저 신고 이력 요약 조회", description = "특정 유저에게 접수된 신고 케이스 집계 및 최근 10건을 조회합니다.")
    public CustomResponse<AdminUserReportSummaryResponse> getUserReportSummary(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long userId
    ) {
        return adminReportUseCase.getUserReportSummary(authUserDetails, userId);
    }

    @GetMapping("/unprocessed-count")
    @Operation(summary = "미처리 신고 케이스 수 조회", description = "처리되지 않은 신고 케이스 수를 조회합니다.")
    public CustomResponse<Long> getUnprocessedCount(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return adminReportUseCase.getUnprocessedCount(authUserDetails);
    }

    @GetMapping("/{reportCaseId}")
    @Operation(summary = "관리자 신고 상세 조회", description = "신고 케이스 요약, 묶인 신고 목록, 피신고 콘텐츠 원문을 조회합니다.")
    public CustomResponse<AdminReportDetailResponse> getReportDetail(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long reportCaseId
    ) {
        return adminReportUseCase.getReportDetail(authUserDetails, reportCaseId);
    }

    @PatchMapping("/{reportCaseId}")
    @Operation(
            summary = "관리자 신고 처리",
            description = "신고 케이스를 처리합니다. status=REJECTED: 반려 / status=COMPLETED + processAction: 처리 완료 및 액션 실행 (CONTENT_DELETED | ACCOUNT_SUSPENDED(7일) | ACCOUNT_DELETED). CONTENT_DELETED는 FEED/FEED_REPLY/REVIEW/CHAT 대상 콘텐츠를 삭제하며, CHAT은 신고된 채팅 메시지 1건을 soft delete합니다."
    )
    public CustomResponse<Void> processReport(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long reportCaseId,
            @Valid @RequestBody AdminReportProcessRequest request
    ) {
        return adminReportUseCase.processReport(authUserDetails, reportCaseId, request);
    }
}
