package com.storix.internal.works.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorksRenameRequest(
        @Schema(description = "지금 저장된 작품명. 다르면 409", example = "19절대역")
        @NotBlank(message = "현재 작품명이 비어 있습니다.")
        String currentName,

        @Schema(description = "바꿀 작품명", example = "절대역")
        @NotBlank(message = "바꿀 작품명이 비어 있습니다.")
        @Size(max = 255, message = "작품명은 255자까지입니다.")
        String newName
) {
}
