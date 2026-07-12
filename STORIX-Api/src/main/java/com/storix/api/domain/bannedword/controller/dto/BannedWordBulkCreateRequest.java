package com.storix.api.domain.bannedword.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BannedWordBulkCreateRequest(
        @Schema(description = "등록할 금칙어 목록", example = "[\"비속어\", \"욕설\"]")
        @NotEmpty(message = "금칙어 목록은 비어있을 수 없습니다.")
        List<@NotBlank(message = "금칙어는 공백일 수 없습니다.")
        @Size(max = 255, message = "금칙어는 255자 이하여야 합니다.") String> words
) {
}
