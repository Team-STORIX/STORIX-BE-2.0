package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.AttendanceCheckAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.dto.AttendanceCheckInResponse;
import com.storix.domain.domains.event.dto.AttendanceStatusResponse;
import com.storix.domain.domains.event.exception.AttendanceAlreadyCheckedInException;
import com.storix.domain.domains.event.exception.AttendanceEventNotActiveException;
import com.storix.domain.domains.event.exception.AttendanceEventNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class AttendanceEventService {

    // 이벤트에 지급표를 지정하지 않았을 때 사용하는 기본값
    // 누적 출석일 → 누적 지급 응모권 (3~6일 1개, 7~11일 2개, 12일 5개)
    private static final NavigableMap<Integer, Integer> DEFAULT_TICKET_TOTALS_BY_ATTENDED_DAYS =
            new TreeMap<>(Map.of(3, 1, 7, 2, 12, 5));

    private final AppEventAdaptor appEventAdaptor;
    private final AttendanceCheckAdaptor attendanceCheckAdaptor;

    @Transactional(readOnly = true)
    public AttendanceStatusResponse getStatus(Long userId, LocalDateTime now) {
        AppEvent event = resolveEvent(now);
        LocalDate today = now.toLocalDate();
        NavigableMap<Integer, Integer> schedule = rewardScheduleOf(event);
        List<LocalDate> attendedDates = attendanceCheckAdaptor.findAttendedDates(event.getId(), userId);
        return AttendanceStatusResponse.builder()
                .appEventId(event.getId())
                .eventStartDate(startDateOf(event))
                .eventEndDate(endDateOf(event))
                .attendedDates(attendedDates)
                .totalAttendedDays(attendedDates.size())
                .attendedToday(attendedDates.contains(today))
                .issuedTickets(issuedTicketsFor(schedule, attendedDates.size()))
                .eventActive(isActiveOn(event, now))
                .build();
    }

    @Transactional
    public AttendanceCheckInResponse checkIn(Long userId, LocalDateTime now) {
        AppEvent event = resolveEvent(now);
        if (!isActiveOn(event, now)) {
            throw AttendanceEventNotActiveException.EXCEPTION;
        }
        LocalDate today = now.toLocalDate();
        if (!attendanceCheckAdaptor.insertIfAbsent(event.getId(), userId, today)) {
            throw AttendanceAlreadyCheckedInException.EXCEPTION;
        }
        NavigableMap<Integer, Integer> schedule = rewardScheduleOf(event);
        int totalAttendedDays = Math.toIntExact(attendanceCheckAdaptor.countAttendedDays(event.getId(), userId));
        int issuedTickets = issuedTicketsFor(schedule, totalAttendedDays);
        return AttendanceCheckInResponse.builder()
                .attendedDate(today)
                .totalAttendedDays(totalAttendedDays)
                .newlyIssuedTickets(issuedTickets - issuedTicketsFor(schedule, totalAttendedDays - 1))
                .issuedTickets(issuedTickets)
                .build();
    }

    // 진행 중인 ATTENDANCE 이벤트를 DB에서 찾는다. 없으면 가장 최근 이벤트로 폴백해
    // 기간 정보를 내려주고 eventActive=false로 응답한다
    private AppEvent resolveEvent(LocalDateTime now) {
        return appEventAdaptor.findActiveOrLatestByType(AppEventType.ATTENDANCE, now)
                .orElseThrow(() -> AttendanceEventNotFoundException.EXCEPTION);
    }

    private boolean isActiveOn(AppEvent event, LocalDateTime now) {
        return !now.isBefore(event.getStartAt()) && now.isBefore(event.getEndAt());
    }

    private static LocalDate startDateOf(AppEvent event) {
        return event.getStartAt().toLocalDate();
    }

    // end_at이 자정(다음 날 00:00, exclusive)으로 저장된 경우 마지막 출석 가능일은 그 전날
    private static LocalDate endDateOf(AppEvent event) {
        LocalDateTime endAt = event.getEndAt();
        return endAt.toLocalTime().equals(LocalTime.MIDNIGHT)
                ? endAt.toLocalDate().minusDays(1)
                : endAt.toLocalDate();
    }

    // 이벤트에 지급표가 지정돼 있으면 그것을, 없으면 기본 지급표를 사용한다
    private static NavigableMap<Integer, Integer> rewardScheduleOf(AppEvent event) {
        Map<Integer, Integer> configured = event.getAttendanceRewards();
        return (configured == null || configured.isEmpty())
                ? DEFAULT_TICKET_TOTALS_BY_ATTENDED_DAYS
                : new TreeMap<>(configured);
    }

    private static int issuedTicketsFor(NavigableMap<Integer, Integer> schedule, int totalAttendedDays) {
        Map.Entry<Integer, Integer> reached = schedule.floorEntry(totalAttendedDays);
        return reached == null ? 0 : reached.getValue();
    }
}
