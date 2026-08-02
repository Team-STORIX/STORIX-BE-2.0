package com.storix.domain.domains.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record AttendanceDrawResponse(

        Long appEventId,

        @Schema(description = "요청한 당첨 인원", example = "10")
        int requestedWinnerCount,

        @Schema(description = "추첨 모수 - 응모권을 1장 이상 보유한 참여자 수", example = "342")
        int candidateCount,

        @Schema(description = "추첨 모수의 총 응모권 수", example = "1088")
        int totalTickets,

        @Schema(description = "당첨자 목록 (1위부터 순위순). 후보가 요청 인원보다 적으면 있는 만큼만 반환합니다.")
        List<AttendanceDrawWinner> winners
) {
}
