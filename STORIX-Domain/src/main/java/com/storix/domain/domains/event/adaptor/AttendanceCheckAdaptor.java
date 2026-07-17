package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.domain.AttendanceCheck;
import com.storix.domain.domains.event.repository.AttendanceCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceCheckAdaptor {

    private final AttendanceCheckRepository attendanceCheckRepository;

    public boolean insertIfAbsent(Long appEventId, Long userId, LocalDate attendedOn) {
        return attendanceCheckRepository.insertIfAbsent(appEventId, userId, attendedOn) > 0;
    }

    public List<LocalDate> findAttendedDates(Long appEventId, Long userId) {
        return attendanceCheckRepository.findAllByAppEventIdAndUserIdOrderByAttendedOnAsc(appEventId, userId)
                .stream()
                .map(AttendanceCheck::getAttendedOn)
                .toList();
    }

    public long countAttendedDays(Long appEventId, Long userId) {
        return attendanceCheckRepository.countByAppEventIdAndUserId(appEventId, userId);
    }
}
