package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.AttendanceCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceCheckRepository extends JpaRepository<AttendanceCheck, Long> {

    List<AttendanceCheck> findAllByAppEventIdAndUserIdOrderByAttendedOnAsc(Long appEventId, Long userId);

    long countByAppEventIdAndUserId(Long appEventId, Long userId);

    // (app_event_id, user_id, attended_on) 유니크 기반 원자적 insert
    // 이미 출석한 날이면 0 반환
    @Modifying
    @Query(value = """
            INSERT IGNORE INTO event_attendance_checks (app_event_id, user_id, attended_on, created_at, updated_at)
            VALUES (:appEventId, :userId, :attendedOn, NOW(), NOW())
            """, nativeQuery = true)
    int insertIfAbsent(@Param("appEventId") Long appEventId,
                       @Param("userId") Long userId,
                       @Param("attendedOn") LocalDate attendedOn);
}
