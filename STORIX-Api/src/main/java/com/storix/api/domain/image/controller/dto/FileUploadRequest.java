package com.storix.api.domain.image.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FileUploadRequest(
        @Schema(description = "업로드할 파일 목록 (1~3개)")
        @NotEmpty(message = "파일을 1개 이상 보내주세요.")
        @Size(min = 1, max = 3, message = "게시글 이미지는 3개까지 업로드 가능합니다.")
        List<@Valid FileInfo> files
) {
    public record FileInfo(
            @Schema(description = "이미지 MIME 타입", example = "image/png")
            @NotBlank(message = "contentType을 보내주세요.")
            String contentType
    ) {}
}
