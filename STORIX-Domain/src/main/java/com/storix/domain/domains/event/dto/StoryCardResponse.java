package com.storix.domain.domains.event.dto;

import com.storix.domain.domains.event.domain.StoryCardDraw;
import com.storix.domain.domains.event.domain.StoryCardGenres;
import com.storix.domain.domains.event.domain.StoryCardGenres.StoryCardImageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder(toBuilder = true)
public record StoryCardResponse(

        @Schema(description = "카드 기준일 (06:00 초기화 기준 서비스 날짜)")
        LocalDate drawnOn,

        @Schema(description = "오늘 이미 뽑아둔 카드를 다시 받은 것인지. true면 '오늘은 이미 참여했어요' 모달을 띄울 수 있다")
        boolean alreadyDrawn,

        @Schema(description = "오늘의 장르", example = "판타지")
        String genre,

        @Schema(description = "장르별 AI 이미지. objectKey로 담겼다가 응답 직전 전체 URL로 치환된다")
        String aiImageUrl,

        @Schema(description = "장르별 배경 이미지. objectKey로 담겼다가 응답 직전 전체 URL로 치환된다")
        String backgroundImageUrl,

        @Schema(description = "장르 아이콘. objectKey로 담겼다가 응답 직전 전체 URL로 치환된다")
        String iconImageUrl,

        @Schema(description = "오늘의 한마디 (줄바꿈을 공백으로 편 한 줄. 공유/저장 텍스트용)")
        String message,

        @Schema(description = "오늘의 한마디를 카드 레이아웃대로 끊은 줄 배열")
        List<String> messageLines,

        @Schema(description = "오늘의 몰입력")
        String immersion,

        @Schema(description = "행운의 작품")
        StoryCardLuckyWorkResponse luckyWork
) {
    public static StoryCardResponse of(StoryCardDraw draw, boolean alreadyDrawn) {
        return StoryCardResponse.builder()
                .drawnOn(draw.getDrawnOn())
                .alreadyDrawn(alreadyDrawn)
                .genre(draw.getGenre().getDbValue())
                .aiImageUrl(StoryCardGenres.imageObjectKeyOf(draw.getGenre(), StoryCardImageType.AI))
                .backgroundImageUrl(StoryCardGenres.imageObjectKeyOf(draw.getGenre(), StoryCardImageType.BACKGROUND))
                .iconImageUrl(StoryCardGenres.imageObjectKeyOf(draw.getGenre(), StoryCardImageType.ICON))
                .message(draw.messageSingleLine())
                .messageLines(draw.messageLines())
                .immersion(draw.getImmersion())
                .luckyWork(StoryCardLuckyWorkResponse.from(draw))
                .build();
    }

    // objectKey → 전체 URL로 변환
    public StoryCardResponse withBaseUrl(String baseUrl) {
        return this.toBuilder()
                .aiImageUrl(prefixed(aiImageUrl, baseUrl))
                .backgroundImageUrl(prefixed(backgroundImageUrl, baseUrl))
                .iconImageUrl(prefixed(iconImageUrl, baseUrl))
                .build();
    }

    private static String prefixed(String objectKey, String baseUrl) {
        if (objectKey == null || objectKey.isBlank()) {
            return objectKey;
        }
        return baseUrl + "/" + objectKey;
    }
}
