package com.storix.api.domain.user.controller;

import com.storix.api.domain.user.controller.dto.AdminUserSanctionRequest;
import com.storix.api.domain.user.usecase.AdminUserUseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.dto.AdminUserContentPageResponse;
import com.storix.domain.domains.user.dto.AdminUserPageResponse;
import com.storix.domain.domains.user.dto.AdminUserSanctionPageResponse;
import com.storix.domain.domains.user.dto.AdminUserSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
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

    @GetMapping("/{userId}/contents")
    @Operation(summary = "관리자 유저 작성 콘텐츠 조회", description = "특정 유저가 작성한 게시글, 댓글, 채팅, 리뷰를 최신순으로 페이지 조회합니다.")
    public CustomResponse<AdminUserContentPageResponse> getUserContents(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "조회할 유저 ID")
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AdminUserContentPageResponse response = adminUserUseCase.getUserContents(authUserDetails, userId, pageable);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, response);
    }

    @PostMapping("/{userId}/sanctions")
    @Operation(
            summary = "관리자 유저 제재 생성",
            description = """
                특정 유저에게 수동 제재를 가하고 계정 상태를 변경합니다.
                type : SUSPENDED(정지)/WITHDRAWN(탈퇴)/RESTORED(복구)/CONTENT_DELETED(콘텐츠 삭제)
                CONTENT_DELETED 처리 시 targetType과 targetId는 필수이며, 그 외 타입에서는 null로 요청합니다.
                """
    )
    public CustomResponse<Void> createUserSanction(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "제재할 유저 ID")
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserSanctionRequest request
    ) {
        adminUserUseCase.createUserSanction(authUserDetails, userId, request);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, null);
    }

    @GetMapping("/{userId}/sanctions")
    @Operation(summary = "관리자 유저 제재 이력 조회", description = "특정 유저의 제재 이력과 제재를 처리한 관리자 정보를 조회합니다.")
    public CustomResponse<AdminUserSanctionPageResponse> getUserSanctions(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "조회할 유저 ID")
            @PathVariable Long userId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AdminUserSanctionPageResponse response = adminUserUseCase.getUserSanctions(authUserDetails, userId, pageable);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, response);
    }
}
