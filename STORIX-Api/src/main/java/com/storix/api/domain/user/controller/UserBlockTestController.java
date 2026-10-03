package com.storix.api.domain.user.controller;

import com.storix.api.domain.user.usecase.UserBlockTestUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/tester")
@RequiredArgsConstructor
@Profile({"local", "dev"})
@Tag(name = "유저 차단", description = "유저 차단 관련 API")
public class UserBlockTestController {

    private final UserBlockTestUseCase userBlockTestUseCase;

    @DeleteMapping("/{targetUserId}/block")
    @Operation(summary = "[테스터] 유저 차단 해제", description = "내가 건 차단을 해제합니다.   \n" +
            "삭제된 건수를 반환합니다. prod 에는 등록되지 않습니다.")
    public CustomResponse<Integer> unblockUser(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable @NotNull Long targetUserId
    ) {
        return userBlockTestUseCase.unblockUser(authUser.getUserId(), targetUserId);
    }
}
