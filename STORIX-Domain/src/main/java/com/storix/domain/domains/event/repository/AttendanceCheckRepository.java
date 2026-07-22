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
    // 중복이면 DataIntegrityViolationException, 그 외 무결성 위반도 그대로 전파된다
    @Modifying
    @Query(value = """
            INSERT INTO event_attendance_checks (app_event_id, user_id, attended_on, created_at, updated_at)
            VALUES (:appEventId, :userId, :attendedOn, NOW(), NOW())
            """, nativeQuery = true)
    int insert(@Param("appEventId") Long appEventId,
               @Param("userId") Long userId,
               @Param("attendedOn") LocalDate attendedOn);
}
