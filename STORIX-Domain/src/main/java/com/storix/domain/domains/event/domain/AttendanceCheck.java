package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(
        name = "event_attendance_checks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_check_event_user_date",
                columnNames = {"app_event_id", "user_id", "attended_on"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceCheck extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_check_id")
    private Long id;

    @Column(name = "app_event_id", nullable = false)
    private Long appEventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 출석 일자 (Asia/Seoul, 00:00~23:59 기준)
    @Column(name = "attended_on", nullable = false)
    private LocalDate attendedOn;

    @Builder
    public AttendanceCheck(Long appEventId, Long userId, LocalDate attendedOn) {
        this.appEventId = appEventId;
        this.userId = userId;
        this.attendedOn = attendedOn;
    }
}
