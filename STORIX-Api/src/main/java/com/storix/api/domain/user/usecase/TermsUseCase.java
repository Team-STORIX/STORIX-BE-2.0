package com.storix.api.domain.user.usecase;

import com.storix.api.domain.user.controller.dto.TermsRegisterRequest;
import com.storix.api.domain.user.controller.dto.TermsRegisterResponse;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.domain.Terms;
import com.storix.domain.domains.user.service.TermsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UseCase
@RequiredArgsConstructor
public class TermsUseCase {

    private final TermsService termsService;

    // 약관 등록
    public ResponseEntity<CustomResponse<TermsRegisterResponse>> registerTerms(TermsRegisterRequest req) {
        Terms saved = termsService.register(req.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.onSuccess(SuccessCode.CREATED, TermsRegisterResponse.from(saved)));
    }
}
