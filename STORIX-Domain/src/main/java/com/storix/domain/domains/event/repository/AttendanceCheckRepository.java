package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.AttendanceCheck;
import com.storix.domain.domains.event.dto.AttendanceAttendeeCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceCheckRepository extends JpaRepository<AttendanceCheck, Long> {

    List<AttendanceCheck> findAllByAppEventIdAndUserIdOrderByAttendedOnAsc(Long appEventId, Long userId);

    long countByAppEventIdAndUserId(Long appEventId, Long userId);

    // 추첨 후보 집계 - 유저별 누적 출석일 수
    // 탈퇴·정지 계정과 관리자 계정은 추첨 대상에서 제외한다
    @Query("""
            SELECT new com.storix.domain.domains.event.dto.AttendanceAttendeeCount(c.userId, COUNT(c))
            FROM AttendanceCheck c
            JOIN User u ON u.id = c.userId
            WHERE c.appEventId = :appEventId
              AND u.accountState = com.storix.domain.domains.user.domain.AccountState.NORMAL
              AND u.role <> com.storix.domain.domains.user.domain.Role.ADMIN
            GROUP BY c.userId
            ORDER BY c.userId ASC
            """)
    List<AttendanceAttendeeCount> findAttendeeCountsByAppEventId(@Param("appEventId") Long appEventId);

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
