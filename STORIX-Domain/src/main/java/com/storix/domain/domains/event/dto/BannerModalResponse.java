package com.storix.domain.domains.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record BannerModalResponse(

        @Schema(description = "해당 배너의 최초 안내 모달을 띄워야 하는지 여부")
        boolean modalRequired
) {
}
