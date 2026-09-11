package com.storix.api.domain.adultverification.controller.dto;

import com.storix.domain.domains.adultverification.domain.AdultVerificationState;
import com.storix.domain.domains.adultverification.dto.AdultVerificationStatusInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdultVerificationStatusResponse(
        @Schema(description = "조회 대상 유저", example = "9")
        Long userId,

        @Schema(description = "NOT_VERIFIED 미인증, VERIFIED 인증 완료, EXPIRED 인증 만료")
        AdultVerificationState state,

        @Schema(description = "인증 화면 진입 가능 여부. 인증 완료 상태에서는 false")
        boolean canVerify,

        @Schema(description = "마지막 인증 시각. 미인증이면 null")
        LocalDateTime verifiedAt,

        @Schema(description = "인증 만료일. 이 날짜까지 유효")
        LocalDate expiresAt
) {

    public static AdultVerificationStatusResponse of(Long userId, AdultVerificationStatusInfo info) {
        return new AdultVerificationStatusResponse(
                userId,
                info.state(),
                info.state().canVerify(),
                info.verifiedAt(),
                info.expiresAt()
        );
    }
}
