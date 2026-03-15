package com.storix.storix_api.domains.image.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileImageUploadRequest(
        @NotNull(message = "업로드할 프로필 사진 정보를 보내주세요.")
        @Valid FileInfo file
) {
    public record FileInfo(
            @NotBlank(message = "contentType을 보내주세요.")
            String contentType
    ) {}
}