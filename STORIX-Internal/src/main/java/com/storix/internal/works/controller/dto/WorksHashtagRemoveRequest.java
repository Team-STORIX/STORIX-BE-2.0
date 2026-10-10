package com.storix.internal.works.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WorksHashtagRemoveRequest(
        @Schema(description = "뺄 해시태그 이름. 앞뒤 공백은 무시", example = "[\"봄티콘\"]")
        @NotEmpty(message = "뺄 해시태그가 없습니다.")
        @Size(max = 100, message = "해시태그는 한 번에 100개까지입니다.")
        List<@NotBlank(message = "해시태그 이름이 비어 있습니다.") String> names
) {
    public List<String> normalizedNames() {
        return names.stream().map(String::trim).distinct().toList();
    }
}
