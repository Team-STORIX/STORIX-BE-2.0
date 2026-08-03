package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventStatus;
import com.storix.domain.domains.event.dto.AppEventCommand;
import com.storix.domain.domains.event.dto.AppEventResponse;
import com.storix.domain.domains.event.exception.AppEventInvalidAttendanceRewardsException;
import com.storix.domain.domains.event.exception.AppEventInvalidPeriodException;
import com.storix.domain.domains.event.exception.AppEventNameRequiredException;
import com.storix.domain.domains.event.exception.AppEventPeriodRequiredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[앱 이벤트] 도메인 서비스 - 생성/수정/강제종료(cascade)/파생 상태")
class AppEventServiceTest {

    private static final Long ID = 100L;
    private static final Long ADMIN_ID = 7L;

    @Mock
    private AppEventAdaptor appEventAdaptor;

    @Mock
    private PopupService popupService;

    @Mock
    private BannerService bannerService;

    @InjectMocks
    private AppEventService appEventService;

    private AppEventCommand command(LocalDateTime startAt, LocalDateTime endAt) {
        return new AppEventCommand("앱 출시 이벤트", "설명", startAt, endAt, false, Set.of(), Map.of());
    }

    private AppEvent appEvent(LocalDateTime startAt, LocalDateTime endAt) {
        AppEvent e = AppEvent.builder()
                .name("앱 출시 이벤트").description("설명")
                .startAt(startAt).endAt(endAt)
                .assigneeAdminId(ADMIN_ID)
                .build();
        ReflectionTestUtils.setField(e, "id", ID);
        return e;
    }

    @Nested
    @DisplayName("create - 커맨드 검증 후 저장")
    class Create {

        @Test
        @DisplayName("정상 커맨드는 담당자와 함께 저장된다")
        void create_ok() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusDays(10);
            given(appEventAdaptor.save(any(AppEvent.class))).willAnswer(inv -> inv.getArgument(0));

            AppEventResponse saved = appEventService.create(command(start, end), ADMIN_ID);

            // 담당자는 응답에 노출되지 않으므로 저장된 엔티티로 검증
            ArgumentCaptor<AppEvent> captor = ArgumentCaptor.forClass(AppEvent.class);
            verify(appEventAdaptor).save(captor.capture());
            assertThat(captor.getValue().getAssigneeAdminId()).isEqualTo(ADMIN_ID);
            assertThat(saved.name()).isEqualTo("앱 출시 이벤트");
        }

        @Test
        @DisplayName("이름이 비면 예외 - 저장하지 않는다")
        void reject_blank_name() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            AppEventCommand cmd = new AppEventCommand("  ", "설명", start, start.plusDays(1), false, Set.of(), Map.of());

            assertThatThrownBy(() -> appEventService.create(cmd, ADMIN_ID))
                    .isInstanceOf(AppEventNameRequiredException.class);
            verify(appEventAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("시작/종료가 null 이면 예외")
        void reject_null_period() {
            assertThatThrownBy(() -> appEventService.create(command(null, null), ADMIN_ID))
                    .isInstanceOf(AppEventPeriodRequiredException.class);
            verify(appEventAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("종료가 시작보다 이르거나 같으면 예외")
        void reject_invalid_period() {
            LocalDateTime start = LocalDateTime.now().plusDays(5);
            assertThatThrownBy(() -> appEventService.create(command(start, start), ADMIN_ID))
                    .isInstanceOf(AppEventInvalidPeriodException.class);
            verify(appEventAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("출석 지급표를 지정하면 그대로 저장한다")
        void create_with_attendance_rewards() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusDays(20);
            Map<Integer, Integer> rewards = Map.of(5, 1, 10, 3, 20, 10);
            AppEventCommand cmd = new AppEventCommand("출석 이벤트", "설명", start, end, false, Set.of(), rewards);
            given(appEventAdaptor.save(any(AppEvent.class))).willAnswer(inv -> inv.getArgument(0));

            AppEventResponse saved = appEventService.create(cmd, ADMIN_ID);

            assertThat(saved.attendanceRewards()).containsExactlyInAnyOrderEntriesOf(rewards);
        }

        @Test
        @DisplayName("누적 응모권이 출석일 증가에 따라 감소하면 예외 - 저장하지 않는다")
        void reject_non_monotonic_rewards() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusDays(20);
            Map<Integer, Integer> rewards = Map.of(5, 3, 10, 1); // 10일차 누적이 5일차보다 작음
            AppEventCommand cmd = new AppEventCommand("출석 이벤트", "설명", start, end, false, Set.of(), rewards);

            assertThatThrownBy(() -> appEventService.create(cmd, ADMIN_ID))
                    .isInstanceOf(AppEventInvalidAttendanceRewardsException.class);
            verify(appEventAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("출석일이 1 미만이면 예외 - 저장하지 않는다")
        void reject_non_positive_day() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusDays(20);
            Map<Integer, Integer> rewards = Map.of(0, 1); // 출석일 0
            AppEventCommand cmd = new AppEventCommand("출석 이벤트", "설명", start, end, false, Set.of(), rewards);

            assertThatThrownBy(() -> appEventService.create(cmd, ADMIN_ID))
                    .isInstanceOf(AppEventInvalidAttendanceRewardsException.class);
            verify(appEventAdaptor, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cancel - 종료 시각을 당기고 팝업/배너 cascade 종료")
    class Cancel {

        @Test
        @DisplayName("cancel 은 endAt 을 현재 이후로 당기고(파생 ENDED) 팝업/배너 종료를 위임한다")
        void cancel_ends_and_cascades() {
            LocalDateTime start = LocalDateTime.now().minusDays(1);
            LocalDateTime end = LocalDateTime.now().plusDays(5);
            given(appEventAdaptor.findById(ID)).willReturn(appEvent(start, end));

            AppEventResponse cancelled = appEventService.cancel(ID);

            // endAt 이 현재로 당겨져 파생 상태가 ENDED
            assertThat(cancelled.status()).isEqualTo(AppEventStatus.ENDED);
            verify(popupService).endByAppEvent(ID);
            verify(bannerService).endByAppEvent(ID);
        }
    }

    @Nested
    @DisplayName("파생 상태 계산 (AppEventStatus.resolve)")
    class DerivedStatus {

        private final LocalDateTime NOW = LocalDateTime.of(2026, 7, 15, 12, 0);

        @Test
        @DisplayName("시작 전이면 SCHEDULED")
        void scheduled_before_start() {
            assertThat(AppEventStatus.resolve(NOW.plusDays(1), NOW.plusDays(5), NOW))
                    .isEqualTo(AppEventStatus.SCHEDULED);
        }

        @Test
        @DisplayName("기간 내면 ACTIVE")
        void active_within() {
            assertThat(AppEventStatus.resolve(NOW.minusDays(1), NOW.plusDays(1), NOW))
                    .isEqualTo(AppEventStatus.ACTIVE);
        }

        @Test
        @DisplayName("종료 시각 도달/경과면 ENDED (경계 포함)")
        void ended_at_or_after_end() {
            assertThat(AppEventStatus.resolve(NOW.minusDays(5), NOW, NOW))
                    .isEqualTo(AppEventStatus.ENDED);
            assertThat(AppEventStatus.resolve(NOW.minusDays(5), NOW.minusDays(1), NOW))
                    .isEqualTo(AppEventStatus.ENDED);
        }
    }
}
