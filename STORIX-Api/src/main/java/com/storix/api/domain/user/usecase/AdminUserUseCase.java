package com.storix.api.domain.user.usecase;

import com.storix.api.domain.user.controller.dto.AdminUserSanctionRequest;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.Role;
import com.storix.domain.domains.user.dto.AdminUserActivityStats;
import com.storix.domain.domains.user.dto.AdminUserBasicInfo;
import com.storix.domain.domains.user.dto.AdminUserListResponse;
import com.storix.domain.domains.user.dto.AdminUserPageResponse;
import com.storix.domain.domains.user.dto.AdminUserReportStats;
import com.storix.domain.domains.user.dto.AdminUserSanctionHistoryResponse;
import com.storix.domain.domains.user.dto.AdminUserSearchCondition;
import com.storix.domain.domains.user.dto.AdminUserSummaryResponse;
import com.storix.domain.domains.user.exception.auth.ForbiddenApproachException;
import com.storix.domain.domains.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class AdminUserUseCase {

    private final AdminUserService adminUserService;

    public AdminUserPageResponse searchUsers(
            AuthUserDetails authUserDetails,
            Long userId,
            String nickName,
            AccountState accountState,
            Pageable pageable
    ) {
        // 관리자 권한 검증
        validateAdmin(authUserDetails);

        // 유저 검색 조건에 따라 유저 목록 조회
        Page<AdminUserListResponse> userInfos = adminUserService.searchUsers(
                new AdminUserSearchCondition(userId, nickName, accountState),
                pageable
        );
        return AdminUserPageResponse.from(userInfos);
    }

    public AdminUserSummaryResponse getUserSummary(AuthUserDetails authUserDetails, Long userId) {

        // 관리자 권한 검증
        validateAdmin(authUserDetails);

        // 유저 기본 사항 조회
        AdminUserBasicInfo basicInfo = adminUserService.getBasicInfo(userId);

        // 유저 활동 통계 조회
        AdminUserActivityStats activityStats = adminUserService.getActivityStats(userId);

        // 유저 신고 통계 조회
        AdminUserReportStats reportStats = adminUserService.getReportStats(userId);

        // 유저 제재 이력 조회
        List<AdminUserSanctionHistoryResponse> sanctions = adminUserService.getSanctionHistories(userId);

        return AdminUserSummaryResponse.of(basicInfo, activityStats, reportStats, sanctions);
    }

    public void createUserSanction(AuthUserDetails authUserDetails, Long userId, AdminUserSanctionRequest request) {

        // 관리자 권한 검증
        validateAdmin(authUserDetails);

        // 유저 제재 처리
        adminUserService.processManualSanction(
                authUserDetails.getUserId(),
                userId,
                request.type(),
                request.memo()
        );
    }

    private void validateAdmin(AuthUserDetails authUserDetails) {
        if (authUserDetails == null || authUserDetails.getRole() != Role.SUPER_ADMIN) {
            throw ForbiddenApproachException.EXCEPTION;
        }
    }
}
