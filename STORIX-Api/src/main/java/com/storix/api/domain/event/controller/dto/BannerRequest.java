package com.storix.api.domain.event.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.api.global.validation.FieldsCompare;
import com.storix.api.global.validation.RequiredIf;
import com.storix.domain.domains.event.domain.ContentTargetType;
import com.storix.domain.domains.event.dto.BannerCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@FieldsCompare(before = "displayStartAt", after = "displayEndAt", field = "displayEndAt",
        message = "노출 종료 일시는 시작 일시 이후여야 합니다.")
@RequiredIf(when = "contentTargetType", is = "APP_EVENT", field = "appEventId",
        message = "APP_EVENT 유형 배너는 앱 이벤트 id(appEventId)가 필수입니다.")
public record BannerRequest(
        @Schema(description = "소속 앱 이벤트 id (선택). 지정 시 노출기간이 이벤트 기간 안으로 제한, 수정 시 무시", example = "10")
        Long appEventId,

        @Schema(description = "콘텐츠 유형", example = "APP_EVENT")
        @NotNull(message = "콘텐츠 유형은 필수입니다.")
        ContentTargetType contentTargetType,

        @Schema(description = "배너 제목", example = "여름맞이 이벤트 배너")
        @NotBlank(message = "배너 제목은 필수입니다.")
        @Size(max = 100, message = "배너 제목은 100자 이하여야 합니다.")
        String bannerTitle,

        @Schema(description = "노출 시작 시각", example = "2026-06-25 00:00", type = "string")
        @NotNull(message = "노출 시작 일시는 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime displayStartAt,

        @Schema(description = "노출 종료 시각", example = "2026-06-30 00:00", type = "string")
        @NotNull(message = "노출 종료 일시는 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime displayEndAt
) {
    public BannerCommand toCommand(String imageObjectKey) {
        return new BannerCommand(appEventId, contentTargetType, bannerTitle, imageObjectKey, displayStartAt, displayEndAt);
    }
}
