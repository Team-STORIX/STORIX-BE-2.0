package com.storix.internal.works.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WorksDeleteRequest(
        @Schema(description = "지울 작품 id. 1~100개, 겹치는 id 는 한 번만 처리", example = "[1817, 2532]")
        @NotEmpty(message = "지울 작품이 없습니다.")
        @Size(max = 100, message = "한 번에 100개까지 지울 수 있습니다.")
        List<@NotNull(message = "작품 id 가 비어 있습니다.") Long> worksIds,

        @Schema(description = "true 면 지우지 않고 지울 데이터와 막는 데이터만 알려줌", example = "true")
        @NotNull(message = "dryRun 을 보내 주세요.")
        Boolean dryRun
) {
    public List<Long> distinctWorksIds() {
        return worksIds.stream().distinct().toList();
    }
}
