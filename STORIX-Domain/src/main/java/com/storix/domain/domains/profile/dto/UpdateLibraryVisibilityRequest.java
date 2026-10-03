package com.storix.domain.domains.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateLibraryVisibilityRequest(
        @Schema(description = "서재 공개 여부", example = "true")
        @NotNull(message = "서재 공개 여부를 선택해주세요.")
        Boolean isPublic
) {
}
