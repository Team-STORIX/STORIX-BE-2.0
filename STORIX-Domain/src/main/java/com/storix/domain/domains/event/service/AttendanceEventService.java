package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.AttendanceCheckAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
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

    // 누적 출석일 → 누적 지급 응모권 (3~6일 1개, 7~13일 2개, 14일 5개)
    private static final NavigableMap<Integer, Integer> TICKET_TOTALS_BY_ATTENDED_DAYS =
            new TreeMap<>(Map.of(3, 1, 7, 2, 14, 5));

    private final AppEventAdaptor appEventAdaptor;
    private final AttendanceCheckAdaptor attendanceCheckAdaptor;

    @Transactional(readOnly = true)
    public AttendanceStatusResponse getStatus(Long appEventId, Long userId, LocalDate today) {
        AppEvent event = resolveEvent(appEventId);
        List<LocalDate> attendedDates = attendanceCheckAdaptor.findAttendedDates(event.getId(), userId);
        return AttendanceStatusResponse.builder()
                .appEventId(event.getId())
                .eventStartDate(startDateOf(event))
                .eventEndDate(endDateOf(event))
                .attendedDates(attendedDates)
                .totalAttendedDays(attendedDates.size())
                .attendedToday(attendedDates.contains(today))
                .issuedTickets(issuedTicketsFor(attendedDates.size()))
                .eventActive(isActiveOn(event, today))
                .build();
    }

    @Transactional
    public AttendanceCheckInResponse checkIn(Long appEventId, Long userId, LocalDate today) {
        AppEvent event = resolveEvent(appEventId);
        if (!isActiveOn(event, today)) {
            throw AttendanceEventNotActiveException.EXCEPTION;
        }
        if (!attendanceCheckAdaptor.insertIfAbsent(event.getId(), userId, today)) {
            throw AttendanceAlreadyCheckedInException.EXCEPTION;
        }
        int totalAttendedDays = (int) attendanceCheckAdaptor.countAttendedDays(event.getId(), userId);
        int issuedTickets = issuedTicketsFor(totalAttendedDays);
        return AttendanceCheckInResponse.builder()
                .attendedDate(today)
                .totalAttendedDays(totalAttendedDays)
                .newlyIssuedTickets(issuedTickets - issuedTicketsFor(totalAttendedDays - 1))
                .issuedTickets(issuedTickets)
                .build();
    }

    private AppEvent resolveEvent(Long appEventId) {
        if (appEventId == null || appEventId <= 0) {
            throw AttendanceEventNotFoundException.EXCEPTION;
        }
        return appEventAdaptor.findOptionalById(appEventId)
                .orElseThrow(() -> AttendanceEventNotFoundException.EXCEPTION);
    }

    private boolean isActiveOn(AppEvent event, LocalDate today) {
        return !today.isBefore(startDateOf(event)) && !today.isAfter(endDateOf(event));
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

    private static int issuedTicketsFor(int totalAttendedDays) {
        Map.Entry<Integer, Integer> reached = TICKET_TOTALS_BY_ATTENDED_DAYS.floorEntry(totalAttendedDays);
        return reached == null ? 0 : reached.getValue();
    }
}
