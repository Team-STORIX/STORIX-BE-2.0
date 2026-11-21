package com.storix.api.domain.aichat.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatMessageRequest(

        @NotBlank
        @Size(max = 500)
        @Schema(description = "질문", example = "계약결혼 로판 중에 완결된 거")
        String question
) {
}
