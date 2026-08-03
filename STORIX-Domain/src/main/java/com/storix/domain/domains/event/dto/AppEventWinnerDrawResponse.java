package com.storix.domain.domains.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record AppEventWinnerDrawResponse(

        Long appEventId,

        @Schema(description = "이번 호출 이전에 이미 확정되어 있었는지 여부. true 면 재추첨 없이 저장된 당첨자를 그대로 반환한 것입니다.", example = "false")
        boolean alreadyFinalized,

        @Schema(description = "확정된 당첨자 목록 (뽑힌 순서). 후보가 요청 인원보다 적으면 있는 만큼만 확정됩니다.")
        List<AppEventDrawWinner> winners
) {
}
