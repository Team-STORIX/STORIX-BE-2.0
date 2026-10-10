package com.storix.internal.works.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WorksMergeRequest(
        @Schema(description = "keep 으로 합치고 지울 작품 id. 1~10개, keep 과 같은 id 불가", example = "[15808, 15868]")
        @NotEmpty(message = "합칠 작품이 없습니다.")
        @Size(max = 10, message = "한 번에 10개까지 합칠 수 있습니다.")
        List<@NotNull(message = "작품 id 가 비어 있습니다.") Long> dropWorksIds
) {
}
