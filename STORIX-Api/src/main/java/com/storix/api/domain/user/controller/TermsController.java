package com.storix.api.domain.user.controller;

import com.storix.api.domain.user.controller.dto.TermsRegisterRequest;
import com.storix.api.domain.user.controller.dto.TermsRegisterResponse;
import com.storix.api.domain.user.usecase.TermsUseCase;
import com.storix.common.payload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
@Tag(name = "약관", description = "약관 관리 API")
public class TermsController {

    private final TermsUseCase termsUseCase;

    @Operation(summary = "[관리자] 약관 등록", description = "버전별로 약관을 등록합니다.  \n" +
            "- termsType: 약관 종류(SERVICE/PRIVACY)  \n" +
            "- 같은 termsType 으로 새 version 을 등록하면, 시행일(effectiveFrom) 기준 가장 최근 버전이 현재 약관으로 사용됩니다.  \n" +
            "- ADMIN 권한 전용입니다.")
    @PostMapping
    public ResponseEntity<CustomResponse<TermsRegisterResponse>> registerTerms(
            @Valid @RequestBody TermsRegisterRequest req
    ) {
        return termsUseCase.registerTerms(req);
    }
}
