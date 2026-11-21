package com.storix.api.domain.aichat.controller.dto;

import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import com.storix.domain.domains.works.domain.WorksType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AiChatSettingResponse(
        @Schema(description = "19금 추천이 실제로 켜져 있는지. 켜 뒀어도 성인 인증이 만료되면 false")
        boolean adultEnabled,
        @Schema(description = "19금 토글을 켤 수 있는지 (지금 성인 인증 유효). false 면 토글을 비활성화")
        boolean adultSelectable,
        @Schema(description = "작품 유형. 빈 배열은 상관없음")
        List<WorksType> worksTypes
) {
    public static AiChatSettingResponse of(AiChatSettingInfo setting, boolean adultVerified) {
        return new AiChatSettingResponse(setting.adultEnabled() && adultVerified, adultVerified,
                setting.worksTypes().stream().sorted().toList());
    }
}
