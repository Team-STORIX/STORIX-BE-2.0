package com.storix.api.domain.adultverification.usecase;

import com.storix.api.domain.adultverification.controller.dto.AdultVerificationStatusResponse;
import com.storix.api.domain.adultverification.controller.dto.AdultVerificationTicketResponse;
import com.storix.infrastructure.external.portone.IdentityVerificationHelper;
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

    // 성인인증 상태 동기화
    public CustomResponse<AdultVerificationStatusResponse> sync(Long userId) {

        // 1. 확정을 기다리는 티켓이 있으면 포트원 상태를 본다. 외부 호출이라 트랜잭션 밖에서 끝낸다
        String pendingId = adultVerificationService.findPendingIdentityVerificationId(userId);
        IdentityVerificationResult pending =
                pendingId == null ? null : identityVerificationHelper.findVerification(pendingId);

        // 2. 인증은 끝났는데 확정이 유실된 건이면 여기서 확정해 상태를 맞춘다
        if (pending != null && pending.isVerified()) {
            log.warn(">>> [AdultVerification] 확정 유실 복구 userId={} identityVerificationId={}", userId, pendingId);
            adultVerificationService.confirm(userId, pendingId, pending);
        }

        return CustomResponse.onSuccess(
                SuccessCode.ADULT_VERIFICATION_STATUS_LOAD_SUCCESS,
                AdultVerificationStatusResponse.of(userId, adultVerificationService.getStatus(userId)));
    }

    // 본인인증 요청 발급
    public CustomResponse<AdultVerificationTicketResponse> issue(Long userId) {

        // 1. 아직 확정되지 않은 티켓이 있으면 포트원 상태부터 본다. 외부 호출이라 트랜잭션 밖에서 끝낸다
        String pendingId = adultVerificationService.findConfirmableIdentityVerificationId(userId);
        // 아직 인증창을 안 띄운 티켓이면 포트원이 모르므로 pending 은 비어 있다
        IdentityVerificationResult pending =
                pendingId == null ? null : identityVerificationHelper.findVerification(pendingId);

        // 2. 인증은 끝났는데 확정이 유실된 건. 여기서 확정해 과금된 인증을 살린다
        // 취소로 실패한 건은 건드리지 않는다. 같은 id 로 인증창이 다시 열린다
        if (pending != null && pending.isVerified()) {
            log.warn(">>> [AdultVerification] 확정 유실 복구 userId={} identityVerificationId={}", userId, pendingId);
            adultVerificationService.confirm(userId, pendingId, pending);
        }

        // 3. 발급. 2번으로 인증이 살아났다면 여기서 409 가 나간다
        AdultVerificationTicketResponse response =
                AdultVerificationTicketResponse.from(adultVerificationService.issue(userId));

        log.info(">>> [AdultVerification] 요청 발급 identityVerificationId={}", response.identityVerificationId());
        return CustomResponse.onSuccess(SuccessCode.ADULT_VERIFICATION_READY_SUCCESS, response);
    }

    // 본인인증 확정
    public CustomResponse<AdultVerificationStatusResponse> confirm(Long userId, String identityVerificationId) {

        // 1. 확정된 건이면 포트원에 묻지 않고 그대로 돌려준다. 연타·재시도에도 같은 응답
        AdultVerificationStatusInfo confirmed =
                adultVerificationService.findConfirmed(userId, identityVerificationId);
        if (confirmed != null) {
            return CustomResponse.onSuccess(
                    SuccessCode.ADULT_VERIFICATION_CONFIRM_SUCCESS,
                    AdultVerificationStatusResponse.of(userId, confirmed));
        }

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
