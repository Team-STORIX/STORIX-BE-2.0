package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.AttendanceCheckAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.dto.AttendanceCheckInResponse;
import com.storix.domain.domains.event.dto.AttendanceStatusResponse;
import com.storix.domain.domains.event.exception.AttendanceAlreadyCheckedInException;
import com.storix.domain.domains.event.exception.AttendanceEventNotActiveException;
import com.storix.domain.domains.event.exception.AttendanceEventNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[출석 이벤트] 도메인 서비스 - 현황 조회/출석 체크/응모권 지급")
class AttendanceEventServiceTest {

    private static final Long EVENT_ID = 100L;
    private static final Long USER_ID = 7L;
    private static final LocalDate START = LocalDate.of(2026, 7, 20);
    private static final LocalDate END = LocalDate.of(2026, 8, 2);

    @Mock
    private AppEventAdaptor appEventAdaptor;

    @Mock
    private AttendanceCheckAdaptor attendanceCheckAdaptor;

    @InjectMocks
    private AttendanceEventService attendanceEventService;

    private AppEvent event(LocalDateTime startAt, LocalDateTime endAt) {
        AppEvent e = AppEvent.builder()
                .name("14일 출석 이벤트").description("설명")
                .startAt(startAt).endAt(endAt)
                .hasWinner(true)
                .build();
        ReflectionTestUtils.setField(e, "id", EVENT_ID);
        return e;
    }

    private AppEvent defaultEvent() {
        return event(START.atStartOfDay(), END.atTime(23, 59));
    }

    private AppEvent eventWithRewards(Map<Integer, Integer> rewards) {
        AppEvent e = AppEvent.builder()
                .name("커스텀 출석 이벤트").description("설명")
                .startAt(START.atStartOfDay()).endAt(END.atTime(23, 59))
                .hasWinner(true)
                .attendanceRewards(rewards)
                .build();
        ReflectionTestUtils.setField(e, "id", EVENT_ID);
        return e;
    }

    @Nested
    @DisplayName("getStatus - 출석 현황 조회")
    class GetStatus {

        @Test
        @DisplayName("출석 날짜/오늘 출석 여부/응모권 수/진행 여부를 한 번에 반환한다")
        void status_ok() {
            LocalDate today = START.plusDays(3);
            List<LocalDate> attended = List.of(START, START.plusDays(1), START.plusDays(3));
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(attendanceCheckAdaptor.findAttendedDates(EVENT_ID, USER_ID)).willReturn(attended);

            AttendanceStatusResponse status = attendanceEventService.getStatus(EVENT_ID, USER_ID, today);

            assertThat(status.appEventId()).isEqualTo(EVENT_ID);
            assertThat(status.eventStartDate()).isEqualTo(START);
            assertThat(status.eventEndDate()).isEqualTo(END);
            assertThat(status.attendedDates()).containsExactlyElementsOf(attended);
            assertThat(status.totalAttendedDays()).isEqualTo(3);
            assertThat(status.attendedToday()).isTrue();
            assertThat(status.issuedTickets()).isEqualTo(1); // 3일 달성
            assertThat(status.eventActive()).isTrue();
        }

        @Test
        @DisplayName("이벤트 기간 외에는 eventActive=false 로 반환한다")
        void status_inactive_after_end() {
            LocalDate today = END.plusDays(1);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(attendanceCheckAdaptor.findAttendedDates(EVENT_ID, USER_ID)).willReturn(List.of());

            AttendanceStatusResponse status = attendanceEventService.getStatus(EVENT_ID, USER_ID, today);

            assertThat(status.eventActive()).isFalse();
            assertThat(status.attendedToday()).isFalse();
            assertThat(status.issuedTickets()).isZero();
        }

        @Test
        @DisplayName("end_at이 자정(exclusive)이면 종료일은 그 전날이다")
        void status_end_at_midnight() {
            given(appEventAdaptor.findOptionalById(EVENT_ID))
                    .willReturn(Optional.of(event(START.atStartOfDay(), END.plusDays(1).atStartOfDay())));
            given(attendanceCheckAdaptor.findAttendedDates(EVENT_ID, USER_ID)).willReturn(List.of());

            AttendanceStatusResponse status = attendanceEventService.getStatus(EVENT_ID, USER_ID, END);

            assertThat(status.eventEndDate()).isEqualTo(END);
            assertThat(status.eventActive()).isTrue();
        }

        @Test
        @DisplayName("이벤트가 설정되지 않았거나(0) 존재하지 않으면 404를 던진다")
        void status_event_not_found() {
            assertThatThrownBy(() -> attendanceEventService.getStatus(0L, USER_ID, START))
                    .isInstanceOf(AttendanceEventNotFoundException.class);

            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.empty());
            assertThatThrownBy(() -> attendanceEventService.getStatus(EVENT_ID, USER_ID, START))
                    .isInstanceOf(AttendanceEventNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("checkIn - 출석 체크")
    class CheckIn {

        @Test
        @DisplayName("정상 출석 시 누적 출석일과 응모권 정보를 반환한다 (마일스톤 미달성)")
        void checkIn_ok_no_milestone() {
            LocalDate today = START.plusDays(1);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(attendanceCheckAdaptor.insertIfAbsent(EVENT_ID, USER_ID, today)).willReturn(true);
            given(attendanceCheckAdaptor.countAttendedDays(EVENT_ID, USER_ID)).willReturn(2L);

            AttendanceCheckInResponse response = attendanceEventService.checkIn(EVENT_ID, USER_ID, today);

            assertThat(response.attendedDate()).isEqualTo(today);
            assertThat(response.totalAttendedDays()).isEqualTo(2);
            assertThat(response.newlyIssuedTickets()).isZero();
            assertThat(response.issuedTickets()).isZero();
        }

        @Test
        @DisplayName("7일 달성 시 누적 응모권이 2개가 된다 (+1)")
        void checkIn_seven_days_issues_ticket() {
            LocalDate today = START.plusDays(6);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(attendanceCheckAdaptor.insertIfAbsent(EVENT_ID, USER_ID, today)).willReturn(true);
            given(attendanceCheckAdaptor.countAttendedDays(EVENT_ID, USER_ID)).willReturn(7L);

            AttendanceCheckInResponse response = attendanceEventService.checkIn(EVENT_ID, USER_ID, today);

            assertThat(response.newlyIssuedTickets()).isEqualTo(1);
            assertThat(response.issuedTickets()).isEqualTo(2);
        }

        @Test
        @DisplayName("14일 달성 시 누적 응모권이 5개가 된다 (+3)")
        void checkIn_fourteen_days_issues_tickets() {
            LocalDate today = END;
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(attendanceCheckAdaptor.insertIfAbsent(EVENT_ID, USER_ID, today)).willReturn(true);
            given(attendanceCheckAdaptor.countAttendedDays(EVENT_ID, USER_ID)).willReturn(14L);

            AttendanceCheckInResponse response = attendanceEventService.checkIn(EVENT_ID, USER_ID, today);

            assertThat(response.newlyIssuedTickets()).isEqualTo(3);
            assertThat(response.issuedTickets()).isEqualTo(5);
        }

        @Test
        @DisplayName("이벤트에 지정된 지급표를 사용해 응모권을 계산한다 (기본표 대신)")
        void checkIn_uses_event_reward_schedule() {
            // 이벤트가 5일차부터 3개 지급하도록 지정 → 기본표(3일 1개)와 다른 결과
            LocalDate today = START.plusDays(4);
            given(appEventAdaptor.findOptionalById(EVENT_ID))
                    .willReturn(Optional.of(eventWithRewards(Map.of(5, 3, 10, 8))));
            given(attendanceCheckAdaptor.insertIfAbsent(EVENT_ID, USER_ID, today)).willReturn(true);
            given(attendanceCheckAdaptor.countAttendedDays(EVENT_ID, USER_ID)).willReturn(5L);

            AttendanceCheckInResponse response = attendanceEventService.checkIn(EVENT_ID, USER_ID, today);

            assertThat(response.newlyIssuedTickets()).isEqualTo(3); // 4일차 0개 → 5일차 3개
            assertThat(response.issuedTickets()).isEqualTo(3);
        }

        @Test
        @DisplayName("이미 출석한 날 재요청 시 409를 던진다")
        void checkIn_duplicate() {
            LocalDate today = START.plusDays(1);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(attendanceCheckAdaptor.insertIfAbsent(EVENT_ID, USER_ID, today)).willReturn(false);

            assertThatThrownBy(() -> attendanceEventService.checkIn(EVENT_ID, USER_ID, today))
                    .isInstanceOf(AttendanceAlreadyCheckedInException.class);
        }

        @Test
        @DisplayName("이벤트 기간 외 출석 요청 시 400을 던지고 출석을 기록하지 않는다")
        void checkIn_not_active() {
            LocalDate today = START.minusDays(1);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));

            assertThatThrownBy(() -> attendanceEventService.checkIn(EVENT_ID, USER_ID, today))
                    .isInstanceOf(AttendanceEventNotActiveException.class);
            verify(attendanceCheckAdaptor, never()).insertIfAbsent(EVENT_ID, USER_ID, today);
        }
    }
}
