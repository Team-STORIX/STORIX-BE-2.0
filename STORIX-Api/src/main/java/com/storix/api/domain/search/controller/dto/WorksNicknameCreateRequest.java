package com.storix.api.domain.search.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorksNicknameCreateRequest(
        @Schema(description = "등록할 작품 별칭", example = "나혼렙")
        @NotBlank(message = "별칭은 필수입니다.")
        @Size(max = 100, message = "별칭은 100자 이하여야 합니다.")
        String nickname
) {
}
