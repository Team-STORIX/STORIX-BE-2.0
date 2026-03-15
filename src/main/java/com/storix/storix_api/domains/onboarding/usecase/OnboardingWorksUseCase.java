package com.storix.storix_api.domains.onboarding.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.onboarding.helper.OnboardingWorksHelper;
import com.storix.storix_api.domains.onboarding.dto.StandardOnboardingWorksInfo;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
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
