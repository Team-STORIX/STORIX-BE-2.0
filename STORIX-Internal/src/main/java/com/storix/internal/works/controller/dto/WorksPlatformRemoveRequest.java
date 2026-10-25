package com.storix.internal.works.controller.dto;

import com.storix.domain.domains.works.domain.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record WorksPlatformRemoveRequest(
        @Schema(description = "뗄 플랫폼. enum 카탈로그의 name", example = "[\"KAKAO_PAGE\"]")
        @NotEmpty(message = "뗄 플랫폼이 없습니다.")
        List<@NotNull(message = "플랫폼이 비어 있습니다.") Platform> platforms,

        @Schema(description = "true 면 떼지 않고 뗄 플랫폼만 알려줌", example = "true")
        @NotNull(message = "dryRun 을 보내 주세요.")
        Boolean dryRun
) {
    public List<Platform> distinctPlatforms() {
        return platforms.stream().distinct().toList();
    }
}
