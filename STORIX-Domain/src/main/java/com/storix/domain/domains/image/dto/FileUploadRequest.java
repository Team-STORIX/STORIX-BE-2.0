package com.storix.domain.domains.image.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FileUploadRequest(
        @NotEmpty(message = "파일을 1개 이상 보내주세요.")
        @Size(min = 1, max = 3, message = "게시글 이미지는 3개까지 업로드 가능합니다.")
        List<@Valid FileInfo> files
) {
    public record FileInfo(
            @NotBlank(message = "contentType을 보내주세요.")
            String contentType
    ) {}
}
