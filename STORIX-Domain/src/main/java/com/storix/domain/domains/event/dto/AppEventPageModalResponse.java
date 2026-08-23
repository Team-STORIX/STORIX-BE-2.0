package com.storix.domain.domains.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AppEventPageModalResponse(

        @Schema(description = "해당 이벤트 상세 페이지의 최초 안내 모달을 띄워야 하는지 여부")
        boolean modalRequired
) {
}
