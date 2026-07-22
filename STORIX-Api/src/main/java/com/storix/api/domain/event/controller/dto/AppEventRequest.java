package com.storix.api.domain.event.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.event.domain.PromotionType;
import com.storix.domain.domains.event.dto.AppEventCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public record AppEventRequest(
        @Schema(description = "앱 이벤트명", example = "앱 출시 기념 출석 체크 이벤트")
        @NotBlank(message = "앱 이벤트명은 필수입니다.")
        @Size(max = 100, message = "앱 이벤트명은 100자 이하여야 합니다.")
        String name,

        @Schema(description = "앱 이벤트 설명", example = "앱 출시를 기념한 첫 이벤트입니다.")
        @Size(max = 500, message = "앱 이벤트 설명은 500자 이하여야 합니다.")
        String description,

        @Schema(description = "이벤트 시작 시각", example = "2026-07-01 00:00")
        @NotNull(message = "이벤트 시작 일시는 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime startAt,

        @Schema(description = "이벤트 종료 시각", example = "2026-07-31 23:59")
        @NotNull(message = "이벤트 종료 일시는 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime endAt,

        @Schema(description = "당첨자 존재 여부", example = "true")
        boolean hasWinner,

        @Schema(description = "홍보 수단, 다중 선택 (PUSH / POPUP / BANNER)", example = "[\"PUSH\", \"POPUP\", \"BANNER\"]")
        Set<PromotionType> promotionTypes,

        @Schema(
                description = "출석 이벤트 응모권 지급 기준. 키=누적 출석일, 값=누적 지급 응모권. 미지정 시 기본표(3일 1개, 7일 2개, 12일 5개) 적용",
                example = "{\"3\": 1, \"7\": 2, \"12\": 5}"
        )
        Map<Integer, Integer> attendanceRewards
) {
    @AssertTrue(message = "이벤트 종료 일시는 시작 일시 이후여야 합니다.")
    private boolean isPeriodValid() {
        if (startAt == null || endAt == null) return true;
        return startAt.isBefore(endAt);
    }

    public AppEventCommand toCommand() {
        return new AppEventCommand(name, description, startAt, endAt, hasWinner, promotionTypes, attendanceRewards);
    }
}
