package com.storix.domain.domains.user.domain;

import com.storix.domain.domains.works.domain.Genre;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// 칭호. 장르 × 단계 조합으로 정의되며, 노출용 칭호명은 displayName 필드로 관리 (칭호명 변경 시 값만 수정)
// 칭호가 정의된 장르만 존재 -> 칭호 없는 개그/액션/스포츠/감성은 부여 대상에서 제외
// 미진입(NONE) 단계는 대표 장르 추적용 -> 칭호명(displayName) 없음(null)
@Getter
@RequiredArgsConstructor
public enum Title {

    // 로맨스
    ROMANCE_NONE(Genre.ROMANCE, TitleStage.NONE, null),
    ROMANCE_ENTRY(Genre.ROMANCE, TitleStage.ENTRY, "풋사랑 독자"),
    ROMANCE_EXPLORE(Genre.ROMANCE, TitleStage.EXPLORE, "두근거림 수집가"),
    ROMANCE_IMMERSE(Genre.ROMANCE, TitleStage.IMMERSE, "사랑 중독자"),

    // 로판
    ROFAN_NONE(Genre.ROFAN, TitleStage.NONE, null),
    ROFAN_ENTRY(Genre.ROFAN, TitleStage.ENTRY, "공작 영애"),
    ROFAN_EXPLORE(Genre.ROFAN, TitleStage.EXPLORE, "사교계의 꽃"),
    ROFAN_IMMERSE(Genre.ROFAN, TitleStage.IMMERSE, "황궁의 실세"),

    // 드라마
    DRAMA_NONE(Genre.DRAMA, TitleStage.NONE, null),
    DRAMA_ENTRY(Genre.DRAMA, TitleStage.ENTRY, "첫 회 시청자"),
    DRAMA_EXPLORE(Genre.DRAMA, TitleStage.EXPLORE, "과몰입 초기"),
    DRAMA_IMMERSE(Genre.DRAMA, TitleStage.IMMERSE, "인생작 수집가"),

    // BL
    BL_NONE(Genre.BL, TitleStage.NONE, null),
    BL_ENTRY(Genre.BL, TitleStage.ENTRY, "벨 초심자"),
    BL_EXPLORE(Genre.BL, TitleStage.EXPLORE, "벨 사냥꾼"),
    BL_IMMERSE(Genre.BL, TitleStage.IMMERSE, "벨 전도사"),

    // 판타지
    FANTASY_NONE(Genre.FANTASY, TitleStage.NONE, null),
    FANTASY_ENTRY(Genre.FANTASY, TitleStage.ENTRY, "길드 신참"),
    FANTASY_EXPLORE(Genre.FANTASY, TitleStage.EXPLORE, "던전 탐험가"),
    FANTASY_IMMERSE(Genre.FANTASY, TitleStage.IMMERSE, "전설의 용사"),

    // 현판
    MODERN_FANTASY_NONE(Genre.MODERN_FANTASY, TitleStage.NONE, null),
    MODERN_FANTASY_ENTRY(Genre.MODERN_FANTASY, TitleStage.ENTRY, "각성자"),
    MODERN_FANTASY_EXPLORE(Genre.MODERN_FANTASY, TitleStage.EXPLORE, "게이트 공략자"),
    MODERN_FANTASY_IMMERSE(Genre.MODERN_FANTASY, TitleStage.IMMERSE, "랭킹권 헌터"),

    // 무협
    HISTORICAL_NONE(Genre.HISTORICAL, TitleStage.NONE, null),
    HISTORICAL_ENTRY(Genre.HISTORICAL, TitleStage.ENTRY, "강호초출"),
    HISTORICAL_EXPLORE(Genre.HISTORICAL, TitleStage.EXPLORE, "일류고수"),
    HISTORICAL_IMMERSE(Genre.HISTORICAL, TitleStage.IMMERSE, "무림지존"),

    // 일상
    DAILY_NONE(Genre.DAILY, TitleStage.NONE, null),
    DAILY_ENTRY(Genre.DAILY, TitleStage.ENTRY, "단골 구경꾼"),
    DAILY_EXPLORE(Genre.DAILY, TitleStage.EXPLORE, "단짝 이웃"),
    DAILY_IMMERSE(Genre.DAILY, TitleStage.IMMERSE, "명예 객식구"),

    // 스릴러
    THRILLER_NONE(Genre.THRILLER, TitleStage.NONE, null),
    THRILLER_ENTRY(Genre.THRILLER, TitleStage.ENTRY, "최초 목격자"),
    THRILLER_EXPLORE(Genre.THRILLER, TitleStage.EXPLORE, "진실 추격자"),
    THRILLER_IMMERSE(Genre.THRILLER, TitleStage.IMMERSE, "최후 생존자");

    private final Genre genre;
    private final TitleStage stage;
    private final String displayName;

    // 칭호가 정의된 장르 집합 (= 칭호 부여 대상 장르)
    public static Set<Genre> titledGenres() {
        return Arrays.stream(values())
                .map(Title::getGenre)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Genre.class)));
    }

    // 장르와 점수로 칭호 해석. 미진입(0~9)도 NONE 항목으로 반환됨(칭호명 null)
    public static Optional<Title> resolve(Genre genre, long score) {
        TitleStage stage = TitleStage.from(score);
        return Arrays.stream(values())
                .filter(t -> t.genre == genre && t.stage == stage)
                .findFirst();
    }

    // 장르와 점수로 획득 가능한 칭호 조회
    public static Set<Title> resolveAcquired(Genre genre, long score) {
        TitleStage currentStage = TitleStage.from(score);
        return Arrays.stream(values())
                .filter(t -> t.genre == genre)
                .filter(t -> t.stage != TitleStage.NONE)
                .filter(t -> t.stage.getStartScore() <= currentStage.getStartScore())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Title.class)));
    }
}
