package com.storix.domain.domains.event.domain;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.works.domain.Genre;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 오늘의 스토리 카드 장르 정의
public final class StoryCardGenres {

    // 전체 URL이 아니라 S3 object key만 갖는다. 앞의 base URL은 AWS_S3_BASE_URL 환경변수로 붙인다
    private static final String IMAGE_KEY_PREFIX = STORIXStatic.S3Prefix.EVENT + "/story-card";

    private static final Map<Genre, String> IMAGE_OBJECT_KEYS = new EnumMap<>(Map.of(
            Genre.ROMANCE,         IMAGE_KEY_PREFIX + "/romance.png",
            Genre.ROFAN,           IMAGE_KEY_PREFIX + "/rofan.png",
            Genre.DRAMA,           IMAGE_KEY_PREFIX + "/drama.png",
            Genre.BL,              IMAGE_KEY_PREFIX + "/bl.png",
            Genre.FANTASY,         IMAGE_KEY_PREFIX + "/fantasy.png",
            Genre.MODERN_FANTASY,  IMAGE_KEY_PREFIX + "/modern-fantasy.png",
            Genre.HISTORICAL,      IMAGE_KEY_PREFIX + "/historical.png",
            Genre.DAILY,           IMAGE_KEY_PREFIX + "/daily.png",
            Genre.THRILLER,        IMAGE_KEY_PREFIX + "/thriller.png"
    ));

    // 랜덤 배정 시 인덱스 접근을 위한 List
    public static final List<Genre> SUPPORTED = List.copyOf(IMAGE_OBJECT_KEYS.keySet());

    private StoryCardGenres() {
    }

    public static Set<Genre> supported() {
        return IMAGE_OBJECT_KEYS.keySet();
    }

    public static boolean isSupported(Genre genre) {
        return genre != null && IMAGE_OBJECT_KEYS.containsKey(genre);
    }

    public static String imageObjectKeyOf(Genre genre) {
        String objectKey = IMAGE_OBJECT_KEYS.get(genre);
        if (objectKey == null) {
            throw new IllegalArgumentException("스토리 카드에서 지원하지 않는 장르입니다: " + genre);
        }
        return objectKey;
    }

    // baseUrl은 @Value("${AWS_S3_BASE_URL}")로 주입받아 넘긴다
    public static String imageUrlOf(Genre genre, String baseUrl) {
        return baseUrl + "/" + imageObjectKeyOf(genre);
    }
}
