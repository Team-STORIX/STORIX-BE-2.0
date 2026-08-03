package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.AttendanceCheckAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.dto.AttendanceAttendeeCount;
import com.storix.domain.domains.event.dto.AttendanceDrawResponse;
import com.storix.domain.domains.event.dto.AttendanceDrawWinner;
import com.storix.domain.domains.event.dto.EventWinner;
import com.storix.domain.domains.event.exception.AttendanceDrawInvalidWinnerCountException;
import com.storix.domain.domains.event.exception.AttendanceEventNotFoundException;
import com.storix.domain.domains.event.service.winner.AppEventFinalizeService;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[출석 이벤트] 당첨자 확정 API - 확정 결과 + 모수 통계 조립")
class AttendanceDrawServiceTest {

    private static final Long EVENT_ID = 100L;
    private static final LocalDate START = LocalDate.of(2026, 7, 20);
    private static final LocalDate END = LocalDate.of(2026, 8, 2);

    @Mock
    private AppEventAdaptor appEventAdaptor;

    @Mock
    private AttendanceCheckAdaptor attendanceCheckAdaptor;

    @Mock
    private AppEventFinalizeService appEventFinalizeService;

    @Mock
    private UserAdaptor userAdaptor;

    @InjectMocks
    private AttendanceDrawService attendanceDrawService;

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
        given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(event(null)));
        given(attendanceCheckAdaptor.findAttendeeCounts(EVENT_ID)).willReturn(List.of(counts));
    }

    private void givenConfirmedWinners(EventWinner... winners) {
        given(appEventFinalizeService.finalizeWinners(anyLong(), anyInt())).willReturn(List.of(winners));
    }

    @Test
    @DisplayName("확정된 당첨자를 뽑힌 순서대로, 닉네임·응모권 수·출석일과 함께 반환한다")
    void draw_returns_confirmed_winners() {
        // 기본 지급표(3일 1개 / 7일 2개 / 12일 5개) 기준
        givenAttendees(
                new AttendanceAttendeeCount(1L, 3L),   // 응모권 1
                new AttendanceAttendeeCount(2L, 12L)   // 응모권 5
        );
        givenConfirmedWinners(new EventWinner(2L, 1), new EventWinner(1L, 2));
        given(userAdaptor.findNicknameMapByUserIds(anyList()))
                .willReturn(Map.of(1L, "닉네임1", 2L, "닉네임2"));

        AttendanceDrawResponse response = attendanceDrawService.draw(EVENT_ID, 2);

        assertThat(response.appEventId()).isEqualTo(EVENT_ID);
        assertThat(response.candidateCount()).isEqualTo(2);
        assertThat(response.totalTickets()).isEqualTo(6); // 1 + 5
        assertThat(response.winners()).extracting(AttendanceDrawWinner::drawOrder).containsExactly(1, 2);
        assertThat(response.winners()).extracting(AttendanceDrawWinner::userId).containsExactly(2L, 1L);

        AttendanceDrawWinner first = response.winners().get(0);
        assertThat(first.nickName()).isEqualTo("닉네임2");
        assertThat(first.ticketCount()).isEqualTo(5);
        assertThat(first.totalAttendedDays()).isEqualTo(12);
    }

    @Test
    @DisplayName("응모권이 0개인 참여자는 모수 통계에서 제외한다")
    void draw_excludes_zero_ticket_attendees_from_stats() {
        givenAttendees(
                new AttendanceAttendeeCount(1L, 2L),   // 3일 미만 → 응모권 0
                new AttendanceAttendeeCount(2L, 7L)    // 응모권 2
        );
        givenConfirmedWinners(new EventWinner(2L, 1));
        given(userAdaptor.findNicknameMapByUserIds(anyList())).willReturn(Map.of(2L, "닉네임2"));

        AttendanceDrawResponse response = attendanceDrawService.draw(EVENT_ID, 5);

        assertThat(response.candidateCount()).isEqualTo(1);
        assertThat(response.totalTickets()).isEqualTo(2);
        assertThat(response.winners()).extracting(AttendanceDrawWinner::userId).containsExactly(2L);
    }

    @Test
    @DisplayName("이벤트에 지정된 지급표로 응모권을 계산한다 (기본표 대신)")
    void draw_uses_event_reward_schedule() {
        given(appEventAdaptor.findOptionalById(EVENT_ID))
                .willReturn(Optional.of(event(Map.of(5, 3, 10, 8))));
        given(attendanceCheckAdaptor.findAttendeeCounts(EVENT_ID))
                .willReturn(List.of(new AttendanceAttendeeCount(1L, 10L)));
        givenConfirmedWinners(new EventWinner(1L, 1));
        given(userAdaptor.findNicknameMapByUserIds(anyList())).willReturn(Map.of(1L, "닉네임1"));

        AttendanceDrawResponse response = attendanceDrawService.draw(EVENT_ID, 1);

        assertThat(response.totalTickets()).isEqualTo(8);
        assertThat(response.winners().get(0).ticketCount()).isEqualTo(8);
    }

    @Test
    @DisplayName("확정된 당첨자가 없으면 빈 목록을 반환한다")
    void draw_without_winners() {
        givenAttendees();
        givenConfirmedWinners();

        AttendanceDrawResponse response = attendanceDrawService.draw(EVENT_ID, 3);

        assertThat(response.candidateCount()).isZero();
        assertThat(response.totalTickets()).isZero();
        assertThat(response.winners()).isEmpty();
    }

    // 확정 이후 출석 데이터가 정리되는 등으로 당첨자가 현재 모수에 없을 수 있다
    @Test
    @DisplayName("당첨자가 현재 모수에 없어도 순서·userId 는 그대로 반환한다")
    void draw_tolerates_winner_missing_from_candidates() {
        givenAttendees(new AttendanceAttendeeCount(2L, 12L));
        givenConfirmedWinners(new EventWinner(1L, 1));
        given(userAdaptor.findNicknameMapByUserIds(anyList())).willReturn(Map.of());

        AttendanceDrawResponse response = attendanceDrawService.draw(EVENT_ID, 1);

        AttendanceDrawWinner winner = response.winners().get(0);
        assertThat(winner.drawOrder()).isEqualTo(1);
        assertThat(winner.userId()).isEqualTo(1L);
        assertThat(winner.nickName()).isNull();
        assertThat(winner.ticketCount()).isZero();
    }

    @Test
    @DisplayName("추첨 인원이 1명 미만이면 400을 던지고 확정하지 않는다")
    void draw_invalid_winner_count() {
        assertThatThrownBy(() -> attendanceDrawService.draw(EVENT_ID, 0))
                .isInstanceOf(AttendanceDrawInvalidWinnerCountException.class);

        verify(appEventAdaptor, never()).findOptionalById(EVENT_ID);
        verify(appEventFinalizeService, never()).finalizeWinners(anyLong(), anyInt());
    }

    @Test
    @DisplayName("존재하지 않는 이벤트면 404를 던진다")
    void draw_event_not_found() {
        assertThatThrownBy(() -> attendanceDrawService.draw(0L, 3))
                .isInstanceOf(AttendanceEventNotFoundException.class);

        given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.empty());
        assertThatThrownBy(() -> attendanceDrawService.draw(EVENT_ID, 3))
                .isInstanceOf(AttendanceEventNotFoundException.class);

        verify(appEventFinalizeService, never()).finalizeWinners(anyLong(), anyInt());
    }

    @Test
    @DisplayName("출석 이벤트가 아니면 404를 던진다 (응모권 개념이 없음)")
    void draw_rejects_non_attendance_event() {
        given(appEventAdaptor.findOptionalById(EVENT_ID))
                .willReturn(Optional.of(event(AppEventType.STORY_CARD, null)));

        assertThatThrownBy(() -> attendanceDrawService.draw(EVENT_ID, 3))
                .isInstanceOf(AttendanceEventNotFoundException.class);

        verify(attendanceCheckAdaptor, never()).findAttendeeCounts(EVENT_ID);
        verify(appEventFinalizeService, never()).finalizeWinners(anyLong(), anyInt());
    }
}
