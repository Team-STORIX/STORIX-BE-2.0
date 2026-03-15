package com.storix.storix_api.domains.plus.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ArtistBoardUploadRequest(

        boolean isWorksSelected,
        Long worksId,

        boolean isContentForFan,
        @Min(value = 100, message = "point는 100 이상이어야 합니다.")
        Integer point,

        @Size(max = 300, message = "게시글은 300자까지 가능합니다.")
        String content,

        @Size(min = 1, max = 3, message = "게시글 이미지는 3개까지 업로드 가능합니다.")
        List<@Valid FileKeys> files
) {
    public record FileKeys(
            @NotBlank(message = "objectKey를 보내주세요.")
            String objectKey
    ) {}

    public List<String> objectKeys() {
        if (files == null) return List.of();
        return files.stream()
                .map(FileKeys::objectKey)
                .filter(k -> k != null && !k.isBlank())
                .toList();
    }
}
