package com.storix.api.domain.user.controller;

import com.storix.api.domain.user.usecase.AdminUserUseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.dto.AdminUserPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
