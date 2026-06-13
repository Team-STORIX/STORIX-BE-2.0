package com.storix.api.domain.profile.controller;

import com.storix.api.domain.profile.usecase.ProfileUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.profile.dto.UserInfoV2;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/profile")
@RequiredArgsConstructor
@Tag(name = "프로필", description = "프로필 관련 API")
public class ProfileV2Controller {

    private final ProfileUseCase profileUseCase;

    @Operation(summary = "기본 프로필 조회 V2",
            description = "칭호와 다음 칭호까지의 진행도(남은 점수/진행률)를 포함한 프로필을 조회하는 api 입니다.")
    @GetMapping("/me")
    public ResponseEntity<CustomResponse<UserInfoV2>> getProfileV2(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return ResponseEntity.ok()
                .body(profileUseCase.getUserProfileV2(authUserDetails));
    }
}
