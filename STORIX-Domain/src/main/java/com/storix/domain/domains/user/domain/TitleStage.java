package com.storix.domain.domains.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum TitleStage {
    // 대표 장르 점수를 4단계로 매핑 (미진입 0~9 / 입문 10~39 / 탐색 40~119 / 몰입 120~)
    NONE("미진입", 0, 10),
    ENTRY("입문", 10, 40),
    EXPLORE("탐색", 40, 120),
    IMMERSE("몰입", 120, null);

    private final String label;
    private final int startScore;
    private final Integer nextScore;

    public static TitleStage from(long score) {
        if (score >= 120) return IMMERSE;
        if (score >= 40) return EXPLORE;
        if (score >= 10) return ENTRY;
        return NONE;
    }

    public boolean isMax() {
        return nextScore == null;
    }

    // 다음 단계
    public Optional<TitleStage> next() {
        return switch (this) {
            case NONE -> Optional.of(ENTRY);
            case ENTRY -> Optional.of(EXPLORE);
            case EXPLORE -> Optional.of(IMMERSE);
            case IMMERSE -> Optional.empty();
        };
    }

    // 비율 = (현재 점수 - 현재 단계 시작 점수) / (다음 단계 기준 점수 - 현재 단계 시작 점수) × 100
    public double progressPercentage(long score) {
        if (isMax()) return 100.0;
        double ratio = (double) (score - startScore) / (nextScore - startScore) * 100.0;
        if (ratio < 0) ratio = 0;
        if (ratio > 100) ratio = 100;
        return Math.round(ratio * 10) / 10.0;
    }
}
