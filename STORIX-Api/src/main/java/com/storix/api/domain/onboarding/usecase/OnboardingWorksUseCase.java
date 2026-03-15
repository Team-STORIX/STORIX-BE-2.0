package com.storix.api.domain.onboarding.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.onboarding.service.OnboardingWorksHelper;
import com.storix.domain.domains.onboarding.dto.StandardOnboardingWorksInfo;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class OnboardingWorksUseCase {

    private final OnboardingWorksHelper onboardingWorksHelper;

    public CustomResponse<List<StandardOnboardingWorksInfo>> findAllOnboardingWorks() {
        List<StandardOnboardingWorksInfo> result = onboardingWorksHelper.findOnboardingWorksList();
        return CustomResponse.onSuccess(SuccessCode.ONBOARDING_WORKS_LIST_LOAD_SUCCESS, result);
    }

}
