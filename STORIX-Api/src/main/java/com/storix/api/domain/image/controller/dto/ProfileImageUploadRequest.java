package com.storix.api.domain.image.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileImageUploadRequest(
        @Schema(description = "업로드할 프로필 사진 정보")
        @NotNull(message = "업로드할 프로필 사진 정보를 보내주세요.")
        @Valid FileInfo file
) {
    public record FileInfo(
            @Schema(description = "이미지 MIME 타입", example = "image/png")
            @NotBlank(message = "contentType을 보내주세요.")
            String contentType
    ) {}
}
