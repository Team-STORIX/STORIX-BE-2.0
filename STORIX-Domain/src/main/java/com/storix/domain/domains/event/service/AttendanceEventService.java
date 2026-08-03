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
import java.util.List;
import java.util.NavigableMap;

@Service
@RequiredArgsConstructor
public class AttendanceEventService {

    private final AppEventAdaptor appEventAdaptor;
    private final AttendanceCheckAdaptor attendanceCheckAdaptor;

    @Transactional(readOnly = true)
    public AttendanceStatusResponse getStatus(Long userId, LocalDateTime now) {
        AppEvent event = resolveEvent(now);
        LocalDate today = event.serviceDateOf(now);
        NavigableMap<Integer, Integer> schedule = AttendanceTicketPolicy.scheduleOf(event);
        List<LocalDate> attendedDates = attendanceCheckAdaptor.findAttendedDates(event.getId(), userId);
        return AttendanceStatusResponse.builder()
                .appEventId(event.getId())
                .eventStartDate(event.participationStartDate())
                .eventEndDate(event.participationEndDate())
                .attendedDates(attendedDates)
                .totalAttendedDays(attendedDates.size())
                .attendedToday(attendedDates.contains(today))
                .issuedTickets(AttendanceTicketPolicy.issuedTicketsFor(schedule, attendedDates.size()))
                .eventActive(event.isActiveAt(now))
                .build();
    }

    @Transactional
    public AttendanceCheckInResponse checkIn(Long userId, LocalDateTime now) {
        AppEvent event = resolveEvent(now);
        if (!event.isActiveAt(now)) {
            throw AttendanceEventNotActiveException.EXCEPTION;
        }
        LocalDate today = event.serviceDateOf(now);
        if (!attendanceCheckAdaptor.insertIfAbsent(event.getId(), userId, today)) {
            throw AttendanceAlreadyCheckedInException.EXCEPTION;
        }
        NavigableMap<Integer, Integer> schedule = AttendanceTicketPolicy.scheduleOf(event);
        int totalAttendedDays = Math.toIntExact(attendanceCheckAdaptor.countAttendedDays(event.getId(), userId));
        int issuedTickets = AttendanceTicketPolicy.issuedTicketsFor(schedule, totalAttendedDays);
        return AttendanceCheckInResponse.builder()
                .attendedDate(today)
                .totalAttendedDays(totalAttendedDays)
                .newlyIssuedTickets(issuedTickets - AttendanceTicketPolicy.issuedTicketsFor(schedule, totalAttendedDays - 1))
                .issuedTickets(issuedTickets)
                .build();
    }

    // 진행 중인 ATTENDANCE 이벤트 검색 및 폴백
    private AppEvent resolveEvent(LocalDateTime now) {
        return appEventAdaptor.findActiveOrNearestByType(AppEventType.ATTENDANCE, now)
                .orElseThrow(() -> AttendanceEventNotFoundException.EXCEPTION);
    }
}
