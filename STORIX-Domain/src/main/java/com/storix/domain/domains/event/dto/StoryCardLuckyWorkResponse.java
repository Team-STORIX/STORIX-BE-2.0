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

        @Schema(description = "작품 페이지 외부 링크. 아직 확보되지 않은 작품은 null이며 이 경우 탭 동작 없이 제목만 노출한다")
        String landingUrl
) {
    public static StoryCardLuckyWorkResponse from(StoryCardDraw draw) {
        return StoryCardLuckyWorkResponse.builder()
                .displayLabel(draw.luckyWorkLabel())
                .worksType(draw.getLuckyWorkType())
                .title(draw.getLuckyWorkTitle())
                .platform(draw.getLuckyWorkPlatform())
                .landingUrl(draw.getLuckyWorkLandingUrl())
                .build();
    }
}
