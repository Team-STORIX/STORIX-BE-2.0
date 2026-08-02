package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.AttendanceCheckAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.dto.AttendanceAttendeeCount;
import com.storix.domain.domains.event.dto.AttendanceDrawResponse;
import com.storix.domain.domains.event.dto.AttendanceDrawWinner;
import com.storix.domain.domains.event.exception.AttendanceDrawInvalidWinnerCountException;
import com.storix.domain.domains.event.exception.AttendanceEventNotFoundException;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.dto.AdminUserContactInfo;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[출석 이벤트] 당첨자 추첨 - 응모권 가중치 랜덤")
class AttendanceDrawServiceTest {

    private static final Long EVENT_ID = 100L;
    private static final LocalDate START = LocalDate.of(2026, 7, 20);
    private static final LocalDate END = LocalDate.of(2026, 8, 2);

    @Mock
    private AppEventAdaptor appEventAdaptor;

    @Mock
    private AttendanceCheckAdaptor attendanceCheckAdaptor;

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

    private static AdminUserContactInfo userInfo(long userId) {
        return new AdminUserContactInfo(
                userId, "닉네임" + userId, "user" + userId + "@storix.kr", "https://cdn/profile" + userId + ".png");
    }

    @Test
    @DisplayName("당첨자를 1위부터 순위순으로, 유저 정보·응모권 수·출석일과 함께 반환한다")
    void draw_returns_ranked_winners() {
        // 기본 지급표(3일 1개 / 7일 2개 / 12일 5개) 기준
        givenAttendees(
                new AttendanceAttendeeCount(1L, 3L),   // 응모권 1
                new AttendanceAttendeeCount(2L, 12L)   // 응모권 5
        );
        given(userAdaptor.findAdminUserContactInfoByUserIds(anyList()))
                .willReturn(Map.of(1L, userInfo(1L), 2L, userInfo(2L)));

        AttendanceDrawResponse response = attendanceDrawService.draw(EVENT_ID, 2);

        assertThat(response.appEventId()).isEqualTo(EVENT_ID);
        assertThat(response.requestedWinnerCount()).isEqualTo(2);
        assertThat(response.candidateCount()).isEqualTo(2);
        assertThat(response.totalTickets()).isEqualTo(6); // 1 + 5
        assertThat(response.winners()).extracting(AttendanceDrawWinner::rank).containsExactly(1, 2);
        assertThat(response.winners()).extracting(AttendanceDrawWinner::userId).containsExactlyInAnyOrder(1L, 2L);

        AttendanceDrawWinner winner = response.winners().stream()
                .filter(w -> w.userId() == 2L)
                .findFirst()
                .orElseThrow();
        assertThat(winner.nickName()).isEqualTo("닉네임2");
        assertThat(winner.email()).isEqualTo("user2@storix.kr");
        assertThat(winner.profileImageUrl()).isEqualTo("https://cdn/profile2.png");
        assertThat(winner.ticketCount()).isEqualTo(5);
        assertThat(winner.totalAttendedDays()).isEqualTo(12);
    }

    @Test
    @DisplayName("응모권이 0개인 참여자는 추첨 모수에서 제외한다")
    void draw_excludes_zero_ticket_attendees() {
        givenAttendees(
                new AttendanceAttendeeCount(1L, 2L),   // 3일 미만 → 응모권 0
                new AttendanceAttendeeCount(2L, 7L)    // 응모권 2
        );
        given(userAdaptor.findAdminUserContactInfoByUserIds(anyList()))
                .willReturn(Map.of(2L, userInfo(2L)));

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
        given(userAdaptor.findAdminUserContactInfoByUserIds(anyList()))
                .willReturn(Map.of(1L, userInfo(1L)));

        AttendanceDrawResponse response = attendanceDrawService.draw(EVENT_ID, 1);

        assertThat(response.totalTickets()).isEqualTo(8);
        assertThat(response.winners().get(0).ticketCount()).isEqualTo(8);
    }

    @Test
    @DisplayName("추첨 모수가 없으면 빈 당첨자 목록을 반환한다")
    void draw_without_candidates() {
        givenAttendees();
        given(userAdaptor.findAdminUserContactInfoByUserIds(List.of())).willReturn(Map.of());

        AttendanceDrawResponse response = attendanceDrawService.draw(EVENT_ID, 3);

        assertThat(response.candidateCount()).isZero();
        assertThat(response.totalTickets()).isZero();
        assertThat(response.winners()).isEmpty();
    }

    @Test
    @DisplayName("유저 정보를 찾지 못해도 순위·userId 는 그대로 반환한다")
    void draw_tolerates_missing_user_info() {
        givenAttendees(new AttendanceAttendeeCount(1L, 12L));
        given(userAdaptor.findAdminUserContactInfoByUserIds(anyList())).willReturn(Map.of());

        AttendanceDrawResponse response = attendanceDrawService.draw(EVENT_ID, 1);

        AttendanceDrawWinner winner = response.winners().get(0);
        assertThat(winner.rank()).isEqualTo(1);
        assertThat(winner.userId()).isEqualTo(1L);
        assertThat(winner.nickName()).isNull();
        assertThat(winner.email()).isNull();
    }

    @Test
    @DisplayName("추첨 인원이 1명 미만이면 400을 던지고 조회하지 않는다")
    void draw_invalid_winner_count() {
        assertThatThrownBy(() -> attendanceDrawService.draw(EVENT_ID, 0))
                .isInstanceOf(AttendanceDrawInvalidWinnerCountException.class);

        verify(appEventAdaptor, never()).findOptionalById(EVENT_ID);
    }

    @Test
    @DisplayName("존재하지 않는 이벤트면 404를 던진다")
    void draw_event_not_found() {
        assertThatThrownBy(() -> attendanceDrawService.draw(0L, 3))
                .isInstanceOf(AttendanceEventNotFoundException.class);

        given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.empty());
        assertThatThrownBy(() -> attendanceDrawService.draw(EVENT_ID, 3))
                .isInstanceOf(AttendanceEventNotFoundException.class);
    }

    @Test
    @DisplayName("출석 이벤트가 아니면 404를 던진다 (응모권 개념이 없음)")
    void draw_rejects_non_attendance_event() {
        given(appEventAdaptor.findOptionalById(EVENT_ID))
                .willReturn(Optional.of(event(AppEventType.STORY_CARD, null)));

        assertThatThrownBy(() -> attendanceDrawService.draw(EVENT_ID, 3))
                .isInstanceOf(AttendanceEventNotFoundException.class);

        verify(attendanceCheckAdaptor, never()).findAttendeeCounts(EVENT_ID);
    }
}
