package com.storix.api.domain.adultverification.controller.dto;

import com.storix.domain.domains.adultverification.dto.AdultVerificationTicket;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdultVerificationTicketResponse(
        @Schema(description = "서버가 발급한 본인인증 번호. SDK 호출과 확정 요청에 그대로 사용")
        String identityVerificationId,

        @Schema(description = "포트원 상점 아이디")
        String storeId,

        @Schema(description = "포트원 채널 키")
        String channelKey
) {

    public static AdultVerificationTicketResponse from(AdultVerificationTicket ticket) {
        return new AdultVerificationTicketResponse(
                ticket.identityVerificationId(),
                ticket.storeId(),
                ticket.channelKey()
        );
    }
}
