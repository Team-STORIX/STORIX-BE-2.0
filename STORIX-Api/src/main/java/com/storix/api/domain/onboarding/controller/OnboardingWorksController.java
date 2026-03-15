package com.storix.api.domain.onboarding.controller;

import com.storix.domain.domains.onboarding.dto.StandardOnboardingWorksInfo;
import com.storix.api.domain.onboarding.usecase.OnboardingWorksUseCase;
import com.storix.common.payload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@Tag(name = "온보딩", description = "온보딩 관련 API")
public class OnboardingWorksController {

    private final OnboardingWorksUseCase onboardingWorksUseCase;

    @Operation(summary = "온보딩 관심 작품 리스트 조회", description = "온보딩 관심 작품 리스트를 조회하는 api 입니다.")
    @GetMapping("/works")
    public ResponseEntity<CustomResponse<List<StandardOnboardingWorksInfo>>> getOnboardingWorks(
    ) {
        return ResponseEntity.ok()
                .body(onboardingWorksUseCase.findAllOnboardingWorks());
    }

}
