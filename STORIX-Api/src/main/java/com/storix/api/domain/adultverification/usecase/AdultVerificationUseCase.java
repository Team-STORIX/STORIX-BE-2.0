package com.storix.api.domain.adultverification.usecase;

import com.storix.api.domain.adultverification.controller.dto.AdultVerificationStatusResponse;
import com.storix.api.domain.adultverification.controller.dto.AdultVerificationTicketResponse;
import com.storix.api.domain.adultverification.helper.IdentityVerificationHelper;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.adultverification.dto.AdultVerificationStatusInfo;
import com.storix.domain.domains.adultverification.dto.IdentityVerificationResult;
import com.storix.domain.domains.adultverification.service.AdultVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class AdultVerificationUseCase {

    private final AdultVerificationService adultVerificationService;
    private final IdentityVerificationHelper identityVerificationHelper;

    // 본인인증 요청 발급
    public CustomResponse<AdultVerificationTicketResponse> issue(Long userId) {
        AdultVerificationTicketResponse response =
                AdultVerificationTicketResponse.from(adultVerificationService.issue(userId));

        log.info(">>> [AdultVerification] 요청 발급 identityVerificationId={}", response.identityVerificationId());
        return CustomResponse.onSuccess(SuccessCode.ADULT_VERIFICATION_READY_SUCCESS, response);
    }

    // 본인인증 확정
    public CustomResponse<AdultVerificationStatusResponse> confirm(Long userId, String identityVerificationId) {

        // 1. 우리가 발급했고 아직 확정되지 않은 건인지
        adultVerificationService.assertConfirmable(userId, identityVerificationId);

        // 2. 앱이 보낸 성공 여부를 믿지 않고 포트원에 다시 묻는다. 트랜잭션 밖에서 처리.
        IdentityVerificationResult result =
                identityVerificationHelper.getVerification(identityVerificationId);

        // 3. 성인 여부를 판정하고 확정
        AdultVerificationStatusInfo info =
                adultVerificationService.confirm(userId, identityVerificationId, result);

        log.info(">>> [AdultVerification] 확정 identityVerificationId={} expiresAt={}",
                identityVerificationId, info.expiresAt());
        return CustomResponse.onSuccess(
                SuccessCode.ADULT_VERIFICATION_CONFIRM_SUCCESS,
                AdultVerificationStatusResponse.of(userId, info));
    }

    // 성인인증 상태 조회
    public CustomResponse<AdultVerificationStatusResponse> getStatus(Long userId) {
        return CustomResponse.onSuccess(
                SuccessCode.ADULT_VERIFICATION_STATUS_LOAD_SUCCESS,
                AdultVerificationStatusResponse.of(userId, adultVerificationService.getStatus(userId)));
    }
}
