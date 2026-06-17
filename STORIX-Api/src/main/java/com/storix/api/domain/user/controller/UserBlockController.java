package com.storix.api.domain.user.controller;

import com.storix.api.domain.user.usecase.UserBlockUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "유저 차단", description = "유저 차단 관련 API")
public class UserBlockController {

    private final UserBlockUseCase userBlockUseCase;

    @Operation(
            summary = "유저 차단",
            description = "특정 유저를 차단합니다. 차단된 유저의 피드, 댓글, 리뷰가 노출되지 않으며 해제할 수 없습니다."
    )
    @PostMapping("/{targetUserId}/block")
    public ResponseEntity<CustomResponse<Void>> blockUser(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long targetUserId
    ) {
        return ResponseEntity.ok()
                .body(userBlockUseCase.blockUser(authUserDetails.getUserId(), targetUserId));
    }
}
