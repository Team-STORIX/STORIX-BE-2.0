package com.storix.api.domain.notification.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.notification.dto.MarketingConsentResponse;
import com.storix.domain.domains.notification.dto.MarketingConsentResult;
import com.storix.domain.domains.notification.service.MarketingConsentService;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class MarketingConsentUseCase {

    private final MarketingConsentService marketingConsentService;

    // 마케팅 수신 동의/거부 처리
    public CustomResponse<MarketingConsentResponse> updateMarketingConsent(Long userId, boolean agreed) {

        MarketingConsentResult result = marketingConsentService.process(userId, agreed);

        String title       = result.agreed() ? STORIXStatic.UserHistory.TITLE_MARKETING_AGREE
                                             : STORIXStatic.UserHistory.TITLE_MARKETING_REJECT;
        String description = result.agreed() ? STORIXStatic.UserHistory.DESC_MARKETING_AGREE
                                             : STORIXStatic.UserHistory.DESC_MARKETING_REJECT;

        return CustomResponse.onSuccess(
                SuccessCode.NOTIFICATION_MARKETING_CONSENT_UPDATE_SUCCESS,
                MarketingConsentResponse.of(result, title, description)
        );
    }
}
