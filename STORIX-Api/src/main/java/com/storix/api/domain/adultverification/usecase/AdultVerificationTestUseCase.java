package com.storix.api.domain.adultverification.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.adultverification.service.AdultVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;

@UseCase
@RequiredArgsConstructor
@Profile({"local", "dev"})
public class AdultVerificationTestUseCase {

    private final AdultVerificationService adultVerificationService;

    public CustomResponse<Integer> deleteHistory(Long targetUserId, Long requesterId) {
        int deleted = adultVerificationService.deleteHistory(targetUserId, requesterId);

        return CustomResponse.onSuccess(SuccessCode.ADULT_VERIFICATION_RESET_SUCCESS, deleted);
    }
}
