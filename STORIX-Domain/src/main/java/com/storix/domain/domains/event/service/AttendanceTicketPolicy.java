package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.domain.AppEvent;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

// 출석 이벤트 응모권 지급표 계산 (출석 체크 / 추첨 공용)
public final class AttendanceTicketPolicy {

    // 이벤트에 지급표를 지정하지 않았을 때 사용하는 기본값
    // 누적 출석일 → 누적 지급 응모권 (3~6일 1개, 7~11일 2개, 12일 5개)
    private static final NavigableMap<Integer, Integer> DEFAULT_TICKET_TOTALS_BY_ATTENDED_DAYS =
            Collections.unmodifiableNavigableMap(new TreeMap<>(Map.of(3, 1, 7, 2, 12, 5)));

    private AttendanceTicketPolicy() {
    }

    // 이벤트에 지급표가 지정돼 있으면 그것을, 없으면 기본 지급표를 사용한다
    public static NavigableMap<Integer, Integer> scheduleOf(AppEvent event) {
        Map<Integer, Integer> configured = event.getAttendanceRewards();
        return (configured == null || configured.isEmpty())
                ? DEFAULT_TICKET_TOTALS_BY_ATTENDED_DAYS
                : new TreeMap<>(configured);
    }

    // 누적 출석일이 달성한 가장 높은 마일스톤의 누적 응모권 수
    public static int issuedTicketsFor(NavigableMap<Integer, Integer> schedule, int totalAttendedDays) {
        Map.Entry<Integer, Integer> reached = schedule.floorEntry(totalAttendedDays);
        return reached == null ? 0 : reached.getValue();
    }
}
