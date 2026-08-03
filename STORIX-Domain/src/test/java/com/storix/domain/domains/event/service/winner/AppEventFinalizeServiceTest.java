package com.storix.domain.domains.event.service.winner;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.dto.AppEventDrawWinner;
import com.storix.domain.domains.event.dto.AppEventWinnerDrawResponse;
import com.storix.domain.domains.event.dto.EventWinner;
import com.storix.domain.domains.event.exception.AppEventInvalidWinnerCountException;
import com.storix.domain.domains.event.exception.AppEventNoWinnerException;
import com.storix.domain.domains.event.exception.EventWinnerFinalizerNotImplementedException;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[앱 이벤트] 당첨자 확정 - 최초 1회 추첨 후 결과 고정")
class AppEventFinalizeServiceTest {

    private static final Long EVENT_ID = 100L;
    private static final LocalDate START = LocalDate.of(2026, 7, 20);
    private static final LocalDate END = LocalDate.of(2026, 8, 2);

    @Mock
    private EventWinnerFinalizer finalizer;

    @Mock
    private AppEventWinnerService appEventWinnerService;

    @Mock
    private AppEventAdaptor appEventAdaptor;

    @Mock
    private UserAdaptor userAdaptor;

    private AppEventFinalizeService appEventFinalizeService;

    @BeforeEach
    void setUp() {
        appEventFinalizeService = new AppEventFinalizeService(
                List.of(finalizer), appEventWinnerService, appEventAdaptor, userAdaptor);
    }

    private AppEvent event(boolean hasWinner) {
        AppEvent e = AppEvent.builder()
                .name("14일 출석 이벤트").description("설명")
                .eventType(AppEventType.ATTENDANCE)
                .startAt(START.atStartOfDay()).endAt(END.plusDays(1).atStartOfDay())
                .hasWinner(hasWinner)
                .build();
        ReflectionTestUtils.setField(e, "id", EVENT_ID);
        return e;
    }

    private void givenEvent(boolean hasWinner) {
        given(appEventAdaptor.findByIdForUpdate(EVENT_ID)).willReturn(event(hasWinner));
    }

    @Test
    @DisplayName("최초 호출이면 추첨해서 저장하고 그 결과를 닉네임과 함께 반환한다")
    void finalizes_on_first_call() {
        List<EventWinner> drawn = List.of(new EventWinner(1L, 1), new EventWinner(2L, 2));
        givenEvent(true);
        given(appEventWinnerService.findWinners(EVENT_ID)).willReturn(List.of());
        given(finalizer.supports(any(AppEvent.class))).willReturn(true);
        given(finalizer.resolveWinners(any(AppEvent.class), eq(2))).willReturn(drawn);
        given(userAdaptor.findNicknameMapByUserIds(anyList()))
                .willReturn(Map.of(1L, "닉네임1", 2L, "닉네임2"));

        AppEventWinnerDrawResponse response = appEventFinalizeService.finalizeWinners(EVENT_ID, 2);

        assertThat(response.appEventId()).isEqualTo(EVENT_ID);
        assertThat(response.alreadyFinalized()).isFalse();
        assertThat(response.winners()).extracting(AppEventDrawWinner::drawOrder).containsExactly(1, 2);
        assertThat(response.winners()).extracting(AppEventDrawWinner::userId).containsExactly(1L, 2L);
        assertThat(response.winners()).extracting(AppEventDrawWinner::nickName).containsExactly("닉네임1", "닉네임2");

        verify(appEventWinnerService).saveWinners(EVENT_ID, drawn);
    }

    // 공정성·감사 근거를 위해 확정된 결과는 다시 뽑지 않는다
    @Test
    @DisplayName("이미 확정된 이벤트는 재추첨하지 않고 저장된 당첨자를 alreadyFinalized=true 로 반환한다")
    void returns_confirmed_winners_without_redrawing() {
        givenEvent(true);
        given(appEventWinnerService.findWinners(EVENT_ID)).willReturn(List.of(new EventWinner(7L, 1)));
        given(userAdaptor.findNicknameMapByUserIds(anyList())).willReturn(Map.of(7L, "닉네임7"));

        AppEventWinnerDrawResponse response = appEventFinalizeService.finalizeWinners(EVENT_ID, 10);

        assertThat(response.alreadyFinalized()).isTrue();
        assertThat(response.winners()).extracting(AppEventDrawWinner::userId).containsExactly(7L);

        verify(finalizer, never()).resolveWinners(any(AppEvent.class), anyInt());
        verify(appEventWinnerService, never()).saveWinners(anyLong(), anyList());
    }

    // 당첨자가 이미 탈퇴한 경우 등
    @Test
    @DisplayName("닉네임을 찾지 못한 당첨자도 순서·userId 는 그대로 반환한다")
    void tolerates_missing_nickname() {
        givenEvent(true);
        given(appEventWinnerService.findWinners(EVENT_ID)).willReturn(List.of(new EventWinner(7L, 1)));
        given(userAdaptor.findNicknameMapByUserIds(anyList())).willReturn(Map.of());

        AppEventDrawWinner winner = appEventFinalizeService.finalizeWinners(EVENT_ID, 10).winners().get(0);

        assertThat(winner.drawOrder()).isEqualTo(1);
        assertThat(winner.userId()).isEqualTo(7L);
        assertThat(winner.nickName()).isNull();
    }

    // 이벤트 유형과 무관한 검증이므로 확정 로직 진입 전에 막는다
    @Test
    @DisplayName("추첨 인원이 1명 미만이면 400을 던지고 이벤트를 조회하지도 않는다")
    void rejects_invalid_winner_count() {
        assertThatThrownBy(() -> appEventFinalizeService.finalizeWinners(EVENT_ID, 0))
                .isInstanceOf(AppEventInvalidWinnerCountException.class);

        verify(appEventAdaptor, never()).findByIdForUpdate(anyLong());
        verify(appEventWinnerService, never()).saveWinners(anyLong(), anyList());
    }

    @Test
    @DisplayName("당첨자를 뽑지 않는 이벤트면 400을 던진다")
    void rejects_event_without_winner() {
        givenEvent(false);

        assertThatThrownBy(() -> appEventFinalizeService.finalizeWinners(EVENT_ID, 3))
                .isInstanceOf(AppEventNoWinnerException.class);

        verify(appEventWinnerService, never()).saveWinners(anyLong(), anyList());
    }

    @Test
    @DisplayName("이벤트 유형을 지원하는 finalizer가 없으면 500을 던진다")
    void rejects_unsupported_event_type() {
        givenEvent(true);
        given(appEventWinnerService.findWinners(EVENT_ID)).willReturn(List.of());
        given(finalizer.supports(any(AppEvent.class))).willReturn(false);

        assertThatThrownBy(() -> appEventFinalizeService.finalizeWinners(EVENT_ID, 3))
                .isInstanceOf(EventWinnerFinalizerNotImplementedException.class);
    }
}
