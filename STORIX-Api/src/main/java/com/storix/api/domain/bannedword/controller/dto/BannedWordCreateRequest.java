package com.storix.api.domain.bannedword.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BannedWordCreateRequest(
        @Schema(description = "등록할 금칙어", example = "비속어")
        @NotBlank(message = "금칙어는 필수입니다.")
        @Size(max = 255, message = "금칙어는 255자 이하여야 합니다.")
        String word
) {
}
