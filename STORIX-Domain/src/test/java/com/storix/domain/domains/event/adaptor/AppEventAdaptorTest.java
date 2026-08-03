package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.repository.AppEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[앱 이벤트] 어댑터 - 진행 중 → 예정 → 종료 폴백")
class AppEventAdaptorTest {

    private static final AppEventType TYPE = AppEventType.ATTENDANCE;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 3, 10, 0);

    @Mock
    private AppEventRepository appEventRepository;

    @InjectMocks
    private AppEventAdaptor appEventAdaptor;

    private AppEvent event(String name) {
        return AppEvent.builder()
                .name(name).description("설명")
                .startAt(NOW).endAt(NOW.plusDays(1))
                .eventType(TYPE)
                .hasWinner(false)
                .build();
    }

    private void givenNoActive() {
        given(appEventRepository.findActiveByType(eq(TYPE), eq(NOW))).willReturn(List.of());
    }

    private void givenNoUpcoming() {
        given(appEventRepository.findFirstByEventTypeAndStartAtGreaterThanOrderByStartAtAsc(eq(TYPE), eq(NOW)))
                .willReturn(Optional.empty());
    }

    @Test
    @DisplayName("진행 중인 이벤트가 있으면 폴백을 타지 않는다")
    void returnsActiveEvent() {
        AppEvent active = event("진행 중 이벤트");
        given(appEventRepository.findActiveByType(eq(TYPE), eq(NOW))).willReturn(List.of(active));

        assertThat(appEventAdaptor.findActiveOrNearestByType(TYPE, NOW)).contains(active);

        verify(appEventRepository, never())
                .findFirstByEventTypeAndStartAtGreaterThanOrderByStartAtAsc(eq(TYPE), eq(NOW));
        verify(appEventRepository, never())
                .findFirstByEventTypeAndEndAtLessThanEqualOrderByEndAtDesc(eq(TYPE), eq(NOW));
    }

    // 종료된 7월 이벤트와 예정된 9월 이벤트가 함께 있는 8/3 시점
    @Test
    @DisplayName("진행 중인 이벤트가 없고 예정 이벤트가 있으면 예정 이벤트를 반환한다")
    void fallsBackToUpcomingEvent() {
        AppEvent upcoming = event("9월 출석 이벤트");
        givenNoActive();
        given(appEventRepository.findFirstByEventTypeAndStartAtGreaterThanOrderByStartAtAsc(eq(TYPE), eq(NOW)))
                .willReturn(Optional.of(upcoming));

        assertThat(appEventAdaptor.findActiveOrNearestByType(TYPE, NOW)).contains(upcoming);

        verify(appEventRepository, never())
                .findFirstByEventTypeAndEndAtLessThanEqualOrderByEndAtDesc(eq(TYPE), eq(NOW));
    }

    @Test
    @DisplayName("진행 중/예정 이벤트가 모두 없으면 가장 최근 종료 이벤트를 반환한다")
    void fallsBackToLatestEndedEvent() {
        AppEvent ended = event("7월 출석 이벤트");
        givenNoActive();
        givenNoUpcoming();
        given(appEventRepository.findFirstByEventTypeAndEndAtLessThanEqualOrderByEndAtDesc(eq(TYPE), eq(NOW)))
                .willReturn(Optional.of(ended));

        assertThat(appEventAdaptor.findActiveOrNearestByType(TYPE, NOW)).contains(ended);
    }

    @Test
    @DisplayName("해당 타입 이벤트가 하나도 없으면 빈 값을 반환한다")
    void returnsEmptyWhenNoEventOfType() {
        givenNoActive();
        givenNoUpcoming();
        given(appEventRepository.findFirstByEventTypeAndEndAtLessThanEqualOrderByEndAtDesc(eq(TYPE), eq(NOW)))
                .willReturn(Optional.empty());

        assertThat(appEventAdaptor.findActiveOrNearestByType(TYPE, NOW)).isEmpty();
    }
}
