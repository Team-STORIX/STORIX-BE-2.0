package com.storix.domain.domains.event.service.winner;

import com.storix.domain.domains.event.adaptor.AttendanceCheckAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.dto.AttendanceAttendeeCount;
import com.storix.domain.domains.event.dto.EventWinner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("[출석 이벤트] 당첨자 산출 - 응모권 가중치 랜덤")
class AttendanceWinnerFinalizerTest {

    private static final Long EVENT_ID = 100L;
    private static final LocalDate START = LocalDate.of(2026, 7, 20);
    private static final LocalDate END = LocalDate.of(2026, 8, 2);

    @Mock
    private AttendanceCheckAdaptor attendanceCheckAdaptor;

    @InjectMocks
    private AttendanceWinnerFinalizer attendanceWinnerFinalizer;

    private AppEvent event(Map<Integer, Integer> rewards) {
        return event(AppEventType.ATTENDANCE, rewards);
    }

    private AppEvent event(AppEventType eventType, Map<Integer, Integer> rewards) {
        AppEvent e = AppEvent.builder()
                .name("14일 출석 이벤트").description("설명")
                .eventType(eventType)
                .startAt(START.atStartOfDay()).endAt(END.plusDays(1).atStartOfDay())
                .hasWinner(true)
                .attendanceRewards(rewards)
                .build();
        ReflectionTestUtils.setField(e, "id", EVENT_ID);
        return e;
    }

    private void givenAttendees(AttendanceAttendeeCount... counts) {
        given(attendanceCheckAdaptor.findAttendeeCounts(EVENT_ID)).willReturn(List.of(counts));
    }

    @Test
    @DisplayName("ATTENDANCE 이벤트만 처리한다")
    void supports_only_attendance_event() {
        assertThat(attendanceWinnerFinalizer.supports(event(null))).isTrue();
        assertThat(attendanceWinnerFinalizer.supports(event(AppEventType.STORY_CARD, null))).isFalse();
        assertThat(attendanceWinnerFinalizer.supports(event(AppEventType.GENERAL, null))).isFalse();
    }

    @Test
    @DisplayName("당첨자를 뽑힌 순서대로 1부터 번호를 매겨 반환한다")
    void resolves_winners_with_draw_order() {
        // 기본 지급표(3일 1개 / 7일 2개 / 12일 5개) 기준
        givenAttendees(
                new AttendanceAttendeeCount(1L, 3L),   // 응모권 1
                new AttendanceAttendeeCount(2L, 12L)   // 응모권 5
        );

        List<EventWinner> winners = attendanceWinnerFinalizer.resolveWinners(event(null), 2);

        assertThat(winners).extracting(EventWinner::drawOrder).containsExactly(1, 2);
        assertThat(winners).extracting(EventWinner::userId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("응모권이 0개인 참여자는 추첨 모수에서 제외한다")
    void excludes_zero_ticket_attendees() {
        givenAttendees(
                new AttendanceAttendeeCount(1L, 2L),   // 3일 미만 → 응모권 0
                new AttendanceAttendeeCount(2L, 7L)    // 응모권 2
        );

        List<EventWinner> winners = attendanceWinnerFinalizer.resolveWinners(event(null), 5);

        assertThat(winners).extracting(EventWinner::userId).containsExactly(2L);
    }

    @Test
    @DisplayName("이벤트에 지정된 지급표로 응모권을 계산한다 (기본표 대신)")
    void uses_event_reward_schedule() {
        // 기본표라면 10일은 응모권 2개지만, 지정표에서는 8개다
        givenAttendees(new AttendanceAttendeeCount(1L, 10L));

        List<EventWinner> winners = attendanceWinnerFinalizer.resolveWinners(event(Map.of(5, 3, 10, 8)), 1);

        assertThat(winners).extracting(EventWinner::userId).containsExactly(1L);
    }

    @Test
    @DisplayName("추첨 모수가 없으면 빈 목록을 반환한다")
    void resolves_empty_without_candidates() {
        givenAttendees();

        assertThat(attendanceWinnerFinalizer.resolveWinners(event(null), 3)).isEmpty();
    }
}
