package com.storix.domain.domains.event.domain;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.event.exception.StoryCardContentNotFoundException;
import com.storix.domain.domains.works.domain.Genre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// 오늘의 스토리 카드 장르 정의
public final class StoryCardGenres {

    // 전체 URL이 아니라 S3 object key만 갖는다. 앞의 base URL은 AWS_S3_BASE_URL 환경변수로 붙인다
    private static final String IMAGE_KEY_PREFIX = STORIXStatic.S3Prefix.EVENT + "/story-card";

    // 카드 이미지 종류. S3 key는 타입 상위 폴더로 구분한다: {prefix}/{type}/{genre}.png
    @Getter
    @RequiredArgsConstructor
    public enum StoryCardImageType {
        AI("ai"),
        BACKGROUND("background"),
        ICON("icon");

        private final String segment;
    }

    private static final Map<Genre, String> GENRE_SLUGS = new EnumMap<>(Map.of(
            Genre.ROMANCE,         "romance",
            Genre.ROFAN,           "rofan",
            Genre.DRAMA,           "drama",
            Genre.BL,              "bl",
            Genre.FANTASY,         "fantasy",
            Genre.MODERN_FANTASY,  "modern-fantasy",
            Genre.HISTORICAL,      "historical",
            Genre.DAILY,           "daily",
            Genre.THRILLER,        "thriller"
    ));

    private static final Map<StoryCardImageType, Map<Genre, String>> IMAGE_OBJECT_KEYS = buildImageObjectKeys();

    // 랜덤 배정 시 인덱스 접근을 위한 List
    public static final List<Genre> SUPPORTED = List.copyOf(GENRE_SLUGS.keySet());

    private StoryCardGenres() {
    }

    private static Map<StoryCardImageType, Map<Genre, String>> buildImageObjectKeys() {
        Map<StoryCardImageType, Map<Genre, String>> byType = new EnumMap<>(StoryCardImageType.class);
        for (StoryCardImageType type : StoryCardImageType.values()) {
            Map<Genre, String> byGenre = new EnumMap<>(Genre.class);
            GENRE_SLUGS.forEach((genre, slug) ->
                    byGenre.put(genre, IMAGE_KEY_PREFIX + "/" + type.getSegment() + "/" + slug + ".png"));
            byType.put(type, byGenre);
        }
        return byType;
    }

    public static boolean isSupported(Genre genre) {
        return genre != null && GENRE_SLUGS.containsKey(genre);
    }

    // 이미 뽑힌 카드의 장르가 나중에 목록에서 빠지면 예외
    public static String imageObjectKeyOf(Genre genre, StoryCardImageType type) {
        String objectKey = IMAGE_OBJECT_KEYS.get(type).get(genre);
        if (objectKey == null) {
            throw StoryCardContentNotFoundException.EXCEPTION;
        }
        return objectKey;
    }

    // baseUrl은 @Value("${AWS_S3_BASE_URL}")로 주입받아 넘긴다
    public static String imageUrlOf(Genre genre, StoryCardImageType type, String baseUrl) {
        return baseUrl + "/" + imageObjectKeyOf(genre, type);
    }
}
