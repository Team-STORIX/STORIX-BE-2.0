package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.PopupAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.ContentTargetType;
import com.storix.domain.domains.event.domain.Popup;
import com.storix.domain.domains.event.domain.PopupExposurePolicy;
import com.storix.domain.domains.event.domain.PopupStatus;
import com.storix.domain.domains.event.dto.PopupCommand;
import com.storix.domain.domains.event.exception.PopupAppEventRequiredException;
import com.storix.domain.domains.event.exception.PopupOutOfEventPeriodException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[이벤트 팝업] 소속 앱 이벤트 검증(APP_EVENT 필수) + 기간 clamp")
class PopupServiceTest {

    private static final Long ADMIN_ID = 7L;
    private static final Long APP_EVENT_ID = 10L;
    private static final Long POPUP_ID = 50L;

    // 이벤트 기간: 기준 ~ 기준+30일
    private static final LocalDateTime EVENT_START = LocalDateTime.of(2026, 7, 1, 0, 0);
    private static final LocalDateTime EVENT_END = EVENT_START.plusDays(30);

    @Mock
    private PopupAdaptor eventPopupAdaptor;

    @Mock
    private AppEventAdaptor appEventAdaptor;

    @Spy
    private EventDisplayPeriodHelper eventDisplayPeriodHelper = new EventDisplayPeriodHelper();

    @InjectMocks
    private PopupService popupService;

    private PopupCommand command(Long appEventId, LocalDateTime start, LocalDateTime end) {
        return new PopupCommand(appEventId, ContentTargetType.APP_EVENT, PopupExposurePolicy.ONCE_PER_DAY, "여름 팝업", "public/event/10/popup/7/img.png", "내용", "자세히", start, end);
    }

    private AppEvent appEvent() {
        AppEvent e = AppEvent.builder()
                .name("앱 출시 이벤트").startAt(EVENT_START).endAt(EVENT_END).assigneeAdminId(ADMIN_ID)
                .build();
        ReflectionTestUtils.setField(e, "id", APP_EVENT_ID);
        return e;
    }

    private Popup existingPopup() {
        Popup p = Popup.builder()
                .appEvent(appEvent())
                .contentTargetType(ContentTargetType.APP_EVENT)
                .exposurePolicy(PopupExposurePolicy.ONCE_PER_DAY)
                .popupTitle("기존").imageObjectKey("public/event/10/popup/7/old.png")
                .displayStartAt(EVENT_START.plusDays(1)).displayEndAt(EVENT_START.plusDays(2))
                .assigneeAdminId(ADMIN_ID)
                .build();
        ReflectionTestUtils.setField(p, "id", POPUP_ID);
        ReflectionTestUtils.setField(p, "status", PopupStatus.ACTIVE);
        return p;
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("팝업 기간이 이벤트 기간 안이면 저장된다")
        void create_within_period() {
            given(appEventAdaptor.findById(APP_EVENT_ID)).willReturn(appEvent());
            given(eventPopupAdaptor.existsOverlapping(any(), any(), eq(null))).willReturn(false);
            given(eventPopupAdaptor.save(any(Popup.class))).willAnswer(inv -> inv.getArgument(0));

            Popup saved = popupService.create(
                    command(APP_EVENT_ID, EVENT_START.plusDays(3), EVENT_START.plusDays(5)), ADMIN_ID);

            assertThat(saved.getAppEvent().getId()).isEqualTo(APP_EVENT_ID);
            verify(eventPopupAdaptor).save(any(Popup.class));        }

        @Test
        @DisplayName("APP_EVENT 유형인데 appEventId 가 null 이면 예외 (소속 이벤트 필수)")
        void reject_app_event_type_without_event() {
            assertThatThrownBy(() -> popupService.create(
                    command(null, EVENT_START.plusDays(3), EVENT_START.plusDays(5)), ADMIN_ID))
                    .isInstanceOf(PopupAppEventRequiredException.class);

            verify(appEventAdaptor, never()).findById(any());
            verify(eventPopupAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("팝업 종료가 이벤트 종료를 넘으면 이벤트 종료로 clamp 되어 저장된다")
        void clamp_end_to_event_end() {
            given(appEventAdaptor.findById(APP_EVENT_ID)).willReturn(appEvent());
            given(eventPopupAdaptor.existsOverlapping(any(), any(), eq(null))).willReturn(false);
            given(eventPopupAdaptor.save(any(Popup.class))).willAnswer(inv -> inv.getArgument(0));

            Popup saved = popupService.create(
                    command(APP_EVENT_ID, EVENT_START.plusDays(3), EVENT_END.plusDays(1)), ADMIN_ID);

            assertThat(saved.getDisplayStartAt()).isEqualTo(EVENT_START.plusDays(3)); // 시작은 그대로
            assertThat(saved.getDisplayEndAt()).isEqualTo(EVENT_END);                 // 종료만 이벤트 종료로 clamp
        }

        @Test
        @DisplayName("팝업 시작이 이벤트 시작보다 이르면 이벤트 시작으로 clamp 되어 저장된다")
        void clamp_start_to_event_start() {
            given(appEventAdaptor.findById(APP_EVENT_ID)).willReturn(appEvent());
            given(eventPopupAdaptor.existsOverlapping(any(), any(), eq(null))).willReturn(false);
            given(eventPopupAdaptor.save(any(Popup.class))).willAnswer(inv -> inv.getArgument(0));

            Popup saved = popupService.create(
                    command(APP_EVENT_ID, EVENT_START.minusDays(1), EVENT_START.plusDays(5)), ADMIN_ID);

            assertThat(saved.getDisplayStartAt()).isEqualTo(EVENT_START);           // 시작만 이벤트 시작으로 clamp
            assertThat(saved.getDisplayEndAt()).isEqualTo(EVENT_START.plusDays(5)); // 종료는 그대로
        }

        @Test
        @DisplayName("팝업 기간이 이벤트 기간과 전혀 겹치지 않으면 예외 (clamp 불가)")
        void reject_when_no_overlap() {
            given(appEventAdaptor.findById(APP_EVENT_ID)).willReturn(appEvent());

            assertThatThrownBy(() -> popupService.create(
                    command(APP_EVENT_ID, EVENT_END.plusDays(1), EVENT_END.plusDays(2)), ADMIN_ID))
                    .isInstanceOf(PopupOutOfEventPeriodException.class);

            verify(eventPopupAdaptor, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update - appEvent 불변, 기존 이벤트 기준")
    class Update {

        @Test
        @DisplayName("수정은 커맨드의 appEventId 를 무시하고 기존 팝업의 이벤트 기간으로 clamp 한다")
        void update_uses_existing_app_event_ignoring_command() {
            given(eventPopupAdaptor.findById(POPUP_ID)).willReturn(existingPopup());
            given(eventPopupAdaptor.existsOverlapping(any(), any(), eq(POPUP_ID))).willReturn(false);

            // 커맨드에 엉뚱한 appEventId(999)를 줘도, 기존 팝업의 이벤트(종료 EVENT_END)로 clamp 된다
            Popup updated = popupService.update(POPUP_ID,
                    command(999L, EVENT_START.plusDays(4), EVENT_END.plusDays(2)));

            assertThat(updated.getAppEvent().getId()).isEqualTo(APP_EVENT_ID); // 이벤트 불변
            assertThat(updated.getDisplayEndAt()).isEqualTo(EVENT_END);        // 기존 이벤트 종료로 clamp
            verify(appEventAdaptor, never()).findById(any());                  // 커맨드 이벤트 로드 안 함
        }

        @Test
        @DisplayName("수정 기간이 이벤트 종료를 넘으면 이벤트 종료로 clamp 된다")
        void clamp_out_of_period_on_update() {
            given(eventPopupAdaptor.findById(POPUP_ID)).willReturn(existingPopup());
            given(eventPopupAdaptor.existsOverlapping(any(), any(), eq(POPUP_ID))).willReturn(false);

            Popup updated = popupService.update(POPUP_ID,
                    command(APP_EVENT_ID, EVENT_START.plusDays(4), EVENT_END.plusDays(2)));

            assertThat(updated.getDisplayStartAt()).isEqualTo(EVENT_START.plusDays(4)); // 시작 그대로
            assertThat(updated.getDisplayEndAt()).isEqualTo(EVENT_END);                 // 종료만 clamp
        }

        @Test
        @DisplayName("종료된 팝업은 수정 시 겹침 검증을 건너뛴다")
        void update_skips_overlap_when_ended() {
            Popup ended = existingPopup();
            ReflectionTestUtils.setField(ended, "status", PopupStatus.ENDED);
            given(eventPopupAdaptor.findById(POPUP_ID)).willReturn(ended);

            popupService.update(POPUP_ID, command(APP_EVENT_ID, EVENT_START.plusDays(4), EVENT_START.plusDays(6)));

            verify(eventPopupAdaptor, never()).existsOverlapping(any(), any(), any());
        }
    }
}
