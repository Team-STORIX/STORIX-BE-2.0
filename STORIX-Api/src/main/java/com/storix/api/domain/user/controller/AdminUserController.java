package com.storix.api.domain.user.controller;

import com.storix.api.domain.user.controller.dto.AdminUserSanctionRequest;
import com.storix.api.domain.user.usecase.AdminUserUseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.dto.AdminUserPageResponse;
import com.storix.domain.domains.user.dto.AdminUserSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "관리자 유저", description = "관리자 유저 관리 API")
public class AdminUserController {

    private final AdminUserUseCase adminUserUseCase;

    @GetMapping
    @Operation(summary = "관리자 유저 목록 조회", description = "유저 ID, 닉네임, 계정 상태로 유저 목록을 조회합니다.")
    public CustomResponse<AdminUserPageResponse> searchUsers(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "검색할 유저 ID")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "검색할 닉네임")
            @RequestParam(required = false) String nickName,
            @Parameter(description = "계정 상태")
            @RequestParam(required = false) AccountState state,
            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준")
            @RequestParam(defaultValue = "createdAt") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        AdminUserPageResponse response = adminUserUseCase.searchUsers(authUserDetails, userId, nickName, state, pageable);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, response);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "관리자 개별 유저 조회", description = "특정 유저의 기본 정보, 활동 통계, 신고 통계, 제재 이력을 조회합니다.")
    public CustomResponse<AdminUserSummaryResponse> getUserSummary(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "조회할 유저 ID")
            @PathVariable Long userId
    ) {
        AdminUserSummaryResponse response = adminUserUseCase.getUserSummary(authUserDetails, userId);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, response);
    }

    @PostMapping("/{userId}/sanctions")
    @Operation(summary = "관리자 유저 제재 생성", description = "특정 유저에게 수동 제재를 생성하고 계정 상태를 변경합니다. type=SUSPENDED는 days 필수, WITHDRAWN/RESTORED는 days를 사용하지 않습니다.")
    public CustomResponse<Void> createUserSanction(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "제재할 유저 ID")
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserSanctionRequest request
    ) {
        adminUserUseCase.createUserSanction(authUserDetails, userId, request);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, null);
    }
}
