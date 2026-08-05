package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.AppEventWinnerAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventStatus;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.dto.AppEventCommand;
import com.storix.domain.domains.event.dto.AppEventResponse;
import com.storix.domain.domains.event.exception.AppEventNotFoundException;
import com.storix.domain.domains.event.dto.AppEventPageResponse;
import com.storix.domain.domains.event.exception.AppEventFinalizedNotModifiableException;
import com.storix.domain.domains.event.exception.AppEventInvalidAttendanceRewardsException;
import com.storix.domain.domains.event.exception.AppEventInvalidPeriodBoundaryException;
import com.storix.domain.domains.event.exception.AppEventInvalidPeriodException;
import com.storix.domain.domains.event.exception.AppEventNameRequiredException;
import com.storix.domain.domains.event.exception.AppEventOverlappingTypeException;
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

import java.time.LocalDate;
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
    private AppEventWinnerAdaptor appEventWinnerAdaptor;

    @Mock
    private PopupService popupService;

    @Mock
    private BannerService bannerService;

    @InjectMocks
    private AppEventService appEventService;

    private AppEventCommand command(LocalDateTime startAt, LocalDateTime endAt) {
        return new AppEventCommand("앱 출시 이벤트", "설명", null, null, startAt, endAt, false, Set.of(), Map.of());
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
    @DisplayName("eventType - 미전달 우회와 동일 타입 기간 중복 검증")
    class EventTypeHandling {

        // 출석은 자정 경계라 기간도 자정에 맞춘다
        private final LocalDateTime START = LocalDate.of(2026, 8, 1).atStartOfDay();
        private final LocalDateTime END = LocalDate.of(2026, 8, 11).atStartOfDay();

        private AppEventCommand typedCommand(AppEventType eventType, LocalDateTime startAt, LocalDateTime endAt) {
            return new AppEventCommand("출석 이벤트", "설명", null, eventType, startAt, endAt, false, Set.of(), Map.of());
        }

        @Test
        @DisplayName("생성 시 미전달(null)이면 GENERAL로 저장하고 중복 검증을 타지 않는다")
        void create_null_type_defaults_to_general() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            given(appEventAdaptor.save(any(AppEvent.class))).willAnswer(inv -> inv.getArgument(0));

            AppEventResponse saved = appEventService.create(command(start, start.plusDays(10)), ADMIN_ID);

            assertThat(saved.eventType()).isEqualTo(AppEventType.GENERAL);
            verify(appEventAdaptor, never()).lockAndCheckOverlappingByType(any(), any(), any(), any());
        }

        @Test
        @DisplayName("수정 시 미전달(null)이면 기존 종류를 유지한다")
        void update_null_type_keeps_existing() {
            AppEvent existing = appEvent(START, END);
            ReflectionTestUtils.setField(existing, "eventType", AppEventType.ATTENDANCE);
            given(appEventAdaptor.findById(ID)).willReturn(existing);

            AppEventResponse updated = appEventService.update(ID, command(START, END));

            assertThat(updated.eventType()).isEqualTo(AppEventType.ATTENDANCE);
        }

        @Test
        @DisplayName("같은 타입 이벤트와 기간이 겹치면 예외 - 저장하지 않는다")
        void reject_overlapping_exclusive_type() {
            given(appEventAdaptor.lockAndCheckOverlappingByType(
                    eq(AppEventType.ATTENDANCE), eq(START), eq(END), eq(null)))
                    .willReturn(true);

            assertThatThrownBy(() ->
                    appEventService.create(typedCommand(AppEventType.ATTENDANCE, START, END), ADMIN_ID))
                    .isInstanceOf(AppEventOverlappingTypeException.class);
            verify(appEventAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("GENERAL은 기간이 겹쳐도 제한하지 않는다")
        void general_type_allows_overlap() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            given(appEventAdaptor.save(any(AppEvent.class))).willAnswer(inv -> inv.getArgument(0));

            appEventService.create(typedCommand(AppEventType.GENERAL, start, start.plusDays(10)), ADMIN_ID);

            verify(appEventAdaptor, never()).lockAndCheckOverlappingByType(any(), any(), any(), any());
            verify(appEventAdaptor).save(any(AppEvent.class));
        }
    }

    @Nested
    @DisplayName("기간 경계 - 이벤트 종류별 기준 시각(출석 00:00 / 스토리 카드 06:00)")
    class PeriodBoundary {

        private final LocalDate DAY = LocalDate.of(2026, 8, 1);

        private AppEventCommand typedCommand(AppEventType eventType, LocalDateTime startAt, LocalDateTime endAt) {
            return new AppEventCommand("이벤트", "설명", null, eventType, startAt, endAt, false, Set.of(), Map.of());
        }

        @Test
        @DisplayName("스토리 카드 기간이 06:00 경계면 저장된다")
        void story_card_reset_hour_boundary_ok() {
            LocalDateTime start = DAY.atTime(6, 0);
            LocalDateTime end = DAY.plusDays(10).atTime(6, 0);
            given(appEventAdaptor.save(any(AppEvent.class))).willAnswer(inv -> inv.getArgument(0));

            appEventService.create(typedCommand(AppEventType.STORY_CARD, start, end), ADMIN_ID);

            verify(appEventAdaptor).save(any(AppEvent.class));
        }

        @Test
        @DisplayName("스토리 카드 기간이 자정이면 예외 - 00:00~06:00 구간에 전날 카드가 나가므로 막는다")
        void reject_story_card_midnight_boundary() {
            LocalDateTime start = DAY.atStartOfDay();
            LocalDateTime end = DAY.plusDays(10).atTime(6, 0);

            assertThatThrownBy(() ->
                    appEventService.create(typedCommand(AppEventType.STORY_CARD, start, end), ADMIN_ID))
                    .isInstanceOf(AppEventInvalidPeriodBoundaryException.class);
            verify(appEventAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("출석 기간이 23:59로 끝나면 예외 - 자정 경계에 맞춰야 한다")
        void reject_attendance_non_midnight_boundary() {
            LocalDateTime start = DAY.atStartOfDay();
            LocalDateTime end = DAY.plusDays(10).atTime(23, 59);

            assertThatThrownBy(() ->
                    appEventService.create(typedCommand(AppEventType.ATTENDANCE, start, end), ADMIN_ID))
                    .isInstanceOf(AppEventInvalidPeriodBoundaryException.class);
            verify(appEventAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("GENERAL은 경계 제약이 없다")
        void general_type_has_no_boundary() {
            LocalDateTime start = DAY.atTime(13, 27);
            given(appEventAdaptor.save(any(AppEvent.class))).willAnswer(inv -> inv.getArgument(0));

            appEventService.create(typedCommand(AppEventType.GENERAL, start, start.plusDays(3)), ADMIN_ID);

            verify(appEventAdaptor).save(any(AppEvent.class));
        }

        @Test
        @DisplayName("기간을 그대로 둔 수정은 경계에 어긋난 기존 이벤트라도 통과한다 - 이름만 바꾸는 수정이 막히면 안 된다")
        void update_keeping_period_skips_boundary_check() {
            // 경계 규칙 이전에 만들어졌거나 cancel()로 end_at이 임의 시각이 된 이벤트
            LocalDateTime start = DAY.atStartOfDay();
            LocalDateTime end = DAY.plusDays(10).atTime(23, 59);
            AppEvent existing = appEvent(start, end);
            ReflectionTestUtils.setField(existing, "eventType", AppEventType.ATTENDANCE);
            given(appEventAdaptor.findById(ID)).willReturn(existing);

            AppEventResponse updated = appEventService.update(ID, typedCommand(AppEventType.ATTENDANCE, start, end));

            assertThat(updated.eventType()).isEqualTo(AppEventType.ATTENDANCE);
        }

        @Test
        @DisplayName("기간을 바꾸는 수정은 경계를 검증한다")
        void update_changing_period_validates_boundary() {
            LocalDateTime start = DAY.atStartOfDay();
            AppEvent existing = appEvent(start, DAY.plusDays(10).atStartOfDay());
            ReflectionTestUtils.setField(existing, "eventType", AppEventType.ATTENDANCE);
            given(appEventAdaptor.findById(ID)).willReturn(existing);

            assertThatThrownBy(() -> appEventService.update(
                    ID, typedCommand(AppEventType.ATTENDANCE, start, DAY.plusDays(20).atTime(23, 59))))
                    .isInstanceOf(AppEventInvalidPeriodBoundaryException.class);
        }

        @Test
        @DisplayName("기간은 그대로여도 종류를 바꾸면 경계를 검증한다 - GENERAL 기간을 출석 이벤트로 전환하는 경우")
        void update_changing_type_validates_boundary() {
            LocalDateTime start = DAY.atTime(13, 27);
            LocalDateTime end = DAY.plusDays(3).atTime(13, 27);
            AppEvent existing = appEvent(start, end);
            given(appEventAdaptor.findById(ID)).willReturn(existing);

            assertThatThrownBy(() ->
                    appEventService.update(ID, typedCommand(AppEventType.ATTENDANCE, start, end)))
                    .isInstanceOf(AppEventInvalidPeriodBoundaryException.class);
        }

        @Test
        @DisplayName("참여 가능 기간은 종료 경계 직전 기준으로 계산한다 - 스토리 카드 08/01 06:00 ~ 08/11 06:00 → 08/01~08/10")
        void participation_dates_follow_boundary() {
            AppEvent event = appEvent(DAY.atTime(6, 0), DAY.plusDays(10).atTime(6, 0));
            ReflectionTestUtils.setField(event, "eventType", AppEventType.STORY_CARD);

            assertThat(event.participationStartDate()).isEqualTo(DAY);
            assertThat(event.participationEndDate()).isEqualTo(DAY.plusDays(9));
        }
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
            AppEventCommand cmd = new AppEventCommand("  ", "설명", null, null, start, start.plusDays(1), false, Set.of(), Map.of());

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
            AppEventCommand cmd = new AppEventCommand("출석 이벤트", "설명", null, null, start, end, false, Set.of(), rewards);
            given(appEventAdaptor.save(any(AppEvent.class))).willAnswer(inv -> inv.getArgument(0));

            AppEventResponse saved = appEventService.create(cmd, ADMIN_ID);

            assertThat(saved.attendanceRewards()).containsExactlyInAnyOrderEntriesOf(rewards);
        }

        // 같은 종류라도 회차마다 다른 화면을 그릴 수 있어야 한다
        @Test
        @DisplayName("페이지 키를 지정하면 그대로 저장하고, 없으면 null 로 둔다")
        void create_with_page_key() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusDays(20);
            given(appEventAdaptor.save(any(AppEvent.class))).willAnswer(inv -> inv.getArgument(0));

            AppEventResponse withKey = appEventService.create(
                    new AppEventCommand("출석 이벤트", "설명", "attendance-2026-08-10", null, start, end, false, Set.of(), Map.of()),
                    ADMIN_ID);
            AppEventResponse withoutKey = appEventService.create(
                    new AppEventCommand("출석 이벤트", "설명", null, null, start, end, false, Set.of(), Map.of()),
                    ADMIN_ID);

            assertThat(withKey.pageKey()).isEqualTo("attendance-2026-08-10");
            assertThat(withoutKey.pageKey()).isNull();
        }
    }

    // 웹페이지용 공개 조회는 인증이 없으므로 노출 범위를 좁게 잡는다
    @Nested
    @DisplayName("getAppEventPage - 이벤트 상세 웹페이지용 공개 조회")
    class GetAppEventPage {

        private AppEvent appEventOf(LocalDateTime startAt, LocalDateTime endAt) {
            AppEvent e = AppEvent.builder()
                    .name("출석 이벤트").description("설명")
                    .pageKey("attendance-2026-08-10")
                    .eventType(AppEventType.ATTENDANCE)
                    .startAt(startAt).endAt(endAt)
                    .build();
            ReflectionTestUtils.setField(e, "id", ID);
            return e;
        }

        @Test
        @DisplayName("진행 중인 이벤트는 종류와 페이지 키를 함께 반환한다")
        void active_event_ok() {
            LocalDateTime now = LocalDateTime.now();
            given(appEventAdaptor.findById(ID)).willReturn(appEventOf(now.minusDays(1), now.plusDays(10)));

            AppEventPageResponse page = appEventService.getAppEventPage(ID);

            assertThat(page.id()).isEqualTo(ID);
            assertThat(page.eventType()).isEqualTo(AppEventType.ATTENDANCE);
            assertThat(page.pageKey()).isEqualTo("attendance-2026-08-10");
            assertThat(page.status()).isEqualTo(AppEventStatus.ACTIVE);
        }

        // 과거 링크로 들어온 유저에게 종료 안내를 보여줄 수 있어야 한다
        @Test
        @DisplayName("종료된 이벤트는 status=ENDED 로 조회된다")
        void ended_event_ok() {
            LocalDateTime now = LocalDateTime.now();
            given(appEventAdaptor.findById(ID)).willReturn(appEventOf(now.minusDays(20), now.minusDays(1)));

            assertThat(appEventService.getAppEventPage(ID).status()).isEqualTo(AppEventStatus.ENDED);
        }

        // id를 훑어 오픈 전 이벤트 내용을 미리 볼 수 있으면 안 된다
        @Test
        @DisplayName("아직 시작하지 않은 이벤트는 404를 던진다")
        void scheduled_event_hidden() {
            LocalDateTime now = LocalDateTime.now();
            given(appEventAdaptor.findById(ID)).willReturn(appEventOf(now.plusDays(3), now.plusDays(10)));

            assertThatThrownBy(() -> appEventService.getAppEventPage(ID))
                    .isInstanceOf(AppEventNotFoundException.class);
        }

        @Test
        @DisplayName("누적 응모권이 출석일 증가에 따라 감소하면 예외 - 저장하지 않는다")
        void reject_non_monotonic_rewards() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusDays(20);
            Map<Integer, Integer> rewards = Map.of(5, 3, 10, 1); // 10일차 누적이 5일차보다 작음
            AppEventCommand cmd = new AppEventCommand("출석 이벤트", "설명", null, null, start, end, false, Set.of(), rewards);

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
            AppEventCommand cmd = new AppEventCommand("출석 이벤트", "설명", null, null, start, end, false, Set.of(), rewards);

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

    // 확정된 당첨자를 어떤 기준으로 뽑았는지 사후에 재현할 수 있어야 한다
    @Nested
    @DisplayName("당첨자 확정 이후 수정 제한")
    class FinalizedImmutability {

        private final LocalDateTime START = LocalDate.of(2026, 8, 1).atStartOfDay();
        private final LocalDateTime END = LocalDate.of(2026, 8, 11).atStartOfDay();

        private AppEventCommand commandOf(String name, LocalDateTime startAt, LocalDateTime endAt,
                                          boolean hasWinner, Map<Integer, Integer> rewards) {
            return new AppEventCommand(name, "설명", null, AppEventType.ATTENDANCE, startAt, endAt, hasWinner, Set.of(), rewards);
        }

        private void givenFinalizedEvent(boolean hasWinner, Map<Integer, Integer> rewards) {
            AppEvent e = AppEvent.builder()
                    .name("출석 이벤트").description("설명")
                    .eventType(AppEventType.ATTENDANCE)
                    .startAt(START).endAt(END)
                    .hasWinner(hasWinner)
                    .attendanceRewards(rewards)
                    .build();
            ReflectionTestUtils.setField(e, "id", ID);
            given(appEventAdaptor.findById(ID)).willReturn(e);
            given(appEventWinnerAdaptor.existsWinner(ID)).willReturn(true);
        }

        @Test
        @DisplayName("기간을 바꾸면 409")
        void rejects_period_change() {
            givenFinalizedEvent(true, Map.of(1, 1));

            assertThatThrownBy(() -> appEventService.update(
                    ID, commandOf("출석 이벤트", START, END.plusDays(7), true, Map.of(1, 1))))
                    .isInstanceOf(AppEventFinalizedNotModifiableException.class);
        }

        @Test
        @DisplayName("응모권 지급표를 바꾸면 409")
        void rejects_rewards_change() {
            givenFinalizedEvent(true, Map.of(1, 1));

            assertThatThrownBy(() -> appEventService.update(
                    ID, commandOf("출석 이벤트", START, END, true, Map.of(1, 5))))
                    .isInstanceOf(AppEventFinalizedNotModifiableException.class);
        }

        // hasWinner를 내려도 당첨자 행은 남아 당첨 알림이 계속 나갈 수 있다
        @Test
        @DisplayName("hasWinner를 내리면 409")
        void rejects_has_winner_change() {
            givenFinalizedEvent(true, Map.of(1, 1));

            assertThatThrownBy(() -> appEventService.update(
                    ID, commandOf("출석 이벤트", START, END, false, Map.of(1, 1))))
                    .isInstanceOf(AppEventFinalizedNotModifiableException.class);
        }

        @Test
        @DisplayName("이름·설명만 바꾸는 수정은 확정 이후에도 허용한다")
        void allows_name_change() {
            givenFinalizedEvent(true, Map.of(1, 1));

            AppEventResponse updated = appEventService.update(
                    ID, commandOf("출석 이벤트 (수정)", START, END, true, Map.of(1, 1)));

            assertThat(updated.name()).isEqualTo("출석 이벤트 (수정)");
        }

        @Test
        @DisplayName("확정 전이면 추첨 근거 값도 자유롭게 바꾼다")
        void allows_everything_before_finalize() {
            AppEvent e = AppEvent.builder()
                    .name("출석 이벤트").description("설명")
                    .eventType(AppEventType.ATTENDANCE)
                    .startAt(START).endAt(END)
                    .hasWinner(true)
                    .attendanceRewards(Map.of(1, 1))
                    .build();
            ReflectionTestUtils.setField(e, "id", ID);
            given(appEventAdaptor.findById(ID)).willReturn(e);
            given(appEventWinnerAdaptor.existsWinner(ID)).willReturn(false);

            AppEventResponse updated = appEventService.update(
                    ID, commandOf("출석 이벤트", START, END.plusDays(7), true, Map.of(1, 5)));

            assertThat(updated.attendanceRewards()).containsEntry(1, 5);
        }
    }
}
