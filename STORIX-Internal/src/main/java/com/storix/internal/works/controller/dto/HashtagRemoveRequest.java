package com.storix.internal.works.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record HashtagRemoveRequest(
        @Schema(description = "모든 작품에서 뺄 해시태그 이름. 앞뒤 공백은 무시", example = "[\"봄티콘\", \"소설원작\"]")
        @NotEmpty(message = "뺄 해시태그가 없습니다.")
        @Size(max = 100, message = "해시태그는 한 번에 100개까지입니다.")
        List<@NotBlank(message = "해시태그 이름이 비어 있습니다.") String> names,

        @Schema(description = "true 면 끊지 않고 붙어 있는 작품만 알려줌", example = "true")
        @NotNull(message = "dryRun 을 보내 주세요.")
        Boolean dryRun
) {
    public List<String> normalizedNames() {
        return names.stream().map(String::trim).distinct().toList();
    }
}
