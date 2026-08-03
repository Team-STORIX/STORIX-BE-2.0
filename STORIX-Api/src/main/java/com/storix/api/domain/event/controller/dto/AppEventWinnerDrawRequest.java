package com.storix.api.domain.event.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AppEventWinnerDrawRequest(

        @Schema(description = "몇 위까지 뽑을지 (당첨 인원)", example = "10")
        @NotNull(message = "추첨 인원은 필수입니다.")
        @Min(value = 1, message = "추첨 인원은 1명 이상이어야 합니다.")
        @Max(value = 1000, message = "추첨 인원은 1000명 이하여야 합니다.")
        Integer winnerCount
) {
}
