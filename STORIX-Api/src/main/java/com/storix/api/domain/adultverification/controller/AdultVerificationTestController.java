package com.storix.api.domain.adultverification.controller;

import com.storix.api.domain.adultverification.controller.dto.AdultVerificationResetRequest;
import com.storix.api.domain.adultverification.usecase.AdultVerificationTestUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/adult-verifications/tester")
@RequiredArgsConstructor
@Profile({"local", "dev"})
@Tag(name = "성인인증", description = "포트원 KG이니시스 통합인증 기반 성인인증 API")
public class AdultVerificationTestController {

    private final AdultVerificationTestUseCase adultVerificationTestUseCase;

    @PostMapping("/reset")
    @Operation(summary = "[테스터] 성인인증 이력 삭제", description = "지정한 userId 의 성인인증 이력을 전부 삭제하고 유저의 인증 시점도 비웁니다.   \n" +
            "삭제된 건수를 반환합니다. prod 에는 등록되지 않습니다.")
    public CustomResponse<Integer> reset(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @RequestBody @Valid AdultVerificationResetRequest request
    ) {
        return adultVerificationTestUseCase.deleteHistory(request.userId(), authUser.getUserId());
    }
}
