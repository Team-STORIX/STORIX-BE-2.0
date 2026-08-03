package com.storix.domain.domains.event.domain;

import com.storix.common.utils.STORIXStatic;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@RequiredArgsConstructor
public enum AppEventType {

    GENERAL("일반", false, null),
    ATTENDANCE("출석 체크", true, LocalTime.MIDNIGHT),
    STORY_CARD("오늘의 스토리 카드", true, LocalTime.of(STORIXStatic.StoryCard.RESET_HOUR, 0));

    private final String description;

    // 같은 시점에 하나만 진행될 수 있는지 여부
    private final boolean exclusive;

    /**
     * 하루가 바뀌는 기준 시각이자 이벤트 기간(start_at / end_at)에 강제되는 경계
     * 출석은 자정, 오늘의 스토리 카드는 06:00에 하루로 바뀌는 걸로 취급
     */
    private final LocalTime boundaryTime;

    // 기간 경계가 이 타입의 기준 시각과 맞는지
    public boolean hasValidBoundary(LocalDateTime at) {
        return boundaryTime == null || at.toLocalTime().equals(boundaryTime);
    }

    // 경계 시각 기준 서비스 날짜. STORY_CARD는 07/28 05:59 → 07/27, 07/28 06:00 → 07/28
    public LocalDate serviceDateOf(LocalDateTime at) {
        return boundaryTime == null
                ? at.toLocalDate()
                : at.minusHours(boundaryTime.getHour()).toLocalDate();
    }

    // 참여 가능한 첫 날
    public LocalDate firstParticipableDate(LocalDateTime startAt) {
        return serviceDateOf(startAt);
    }

    // 참여 가능한 마지막 날. end_at은 exclusive 경계라 그 직전 시점의 서비스 날짜가 마지막 날이다
    public LocalDate lastParticipableDate(LocalDateTime endAt) {
        return serviceDateOf(endAt.minusNanos(1));
    }
}
