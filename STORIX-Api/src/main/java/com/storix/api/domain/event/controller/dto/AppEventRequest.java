package com.storix.api.domain.event.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.domain.PromotionType;
import com.storix.domain.domains.event.dto.AppEventCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

        @Schema(
                description = "이벤트 상세 웹페이지가 그릴 화면을 고르는 키. 같은 종류라도 회차마다 구성이 바뀌면 다른 값을 넣습니다. "
                        + "비우면 종류별 기본 화면을 사용합니다. 쓸 수 있는 값은 프론트와 맞춰주세요.",
                example = "attendance-2026-08-10"
        )
        @Size(max = 50, message = "페이지 키는 50자 이하여야 합니다.")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "페이지 키는 영소문자, 숫자, 하이픈만 사용할 수 있습니다.")
        String pageKey,

        @Schema(
                description = "이벤트 종류. ATTENDANCE / STORY_CARD는 전용 API가 이 값으로 진행 중인 이벤트를 찾으므로 "
                        + "같은 타입끼리 기간이 겹칠 수 없습니다. 특별한 종류가 없으면 GENERAL",
                example = "STORY_CARD"
        )
        @NotNull(message = "이벤트 종류는 필수입니다.")
        AppEventType eventType,

        @Schema(
                description = "이벤트 시작 시각. ATTENDANCE는 00:00, STORY_CARD는 06:00(카드 초기화 시각)에 맞춰야 합니다.",
                example = "2026-07-01 00:00", type = "string"
        )
        @NotNull(message = "이벤트 시작 일시는 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime startAt,

        @Schema(
                description = "이벤트 종료 시각(exclusive). 시작 시각과 같은 경계에 맞춰야 하며, "
                        + "마지막 참여일의 다음 경계를 넣습니다. (7/31까지 출석 → 2026-08-01 00:00)",
                example = "2026-08-01 00:00", type = "string"
        )
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
        return new AppEventCommand(name, description, pageKey, eventType, startAt, endAt, hasWinner, promotionTypes, attendanceRewards);
    }
}
