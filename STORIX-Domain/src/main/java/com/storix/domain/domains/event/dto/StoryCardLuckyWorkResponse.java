package com.storix.domain.domains.event.dto;

import com.storix.domain.domains.event.domain.StoryCardDraw;
import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.WorksType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record StoryCardLuckyWorkResponse(

        @Schema(description = "카드에 그대로 노출되는 라벨", example = "웹툰 ㅣ 화산귀환")
        String displayLabel,

        @Schema(description = "작품 유형")
        WorksType worksType,

        String title,

        @Schema(description = "연재 플랫폼")
        Platform platform,

        @Schema(description = "작품 상세 페이지로 이동할 때 쓰는 앱 내부 작품 ID")
        Long luckyWorkId
) {
    public static StoryCardLuckyWorkResponse from(StoryCardDraw draw) {
        return StoryCardLuckyWorkResponse.builder()
                .displayLabel(draw.luckyWorkLabel())
                .worksType(draw.getLuckyWorkType())
                .title(draw.getLuckyWorkTitle())
                .platform(draw.getLuckyWorkPlatform())
                .luckyWorkId(draw.getLuckyWorkId())
                .build();
    }
}
