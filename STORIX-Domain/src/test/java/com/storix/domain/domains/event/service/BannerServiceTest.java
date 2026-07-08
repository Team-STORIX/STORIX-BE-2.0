package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.BannerAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.Banner;
import com.storix.domain.domains.event.domain.BannerStatus;
import com.storix.domain.domains.event.domain.ContentTargetType;
import com.storix.domain.domains.event.dto.BannerCommand;
import com.storix.domain.domains.event.dto.DisplayPeriod;
import com.storix.domain.domains.event.exception.BannerAppEventRequiredException;
import com.storix.domain.domains.event.exception.BannerOutOfEventPeriodException;
import com.storix.domain.domains.event.exception.BannerOverlappingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[이벤트 배너] 소속 앱 이벤트 검증(APP_EVENT 필수) + 기간 clamp + 동시 노출 최대 3개")
class BannerServiceTest {

    private static final Long ADMIN_ID = 7L;
    private static final Long APP_EVENT_ID = 10L;
    private static final Long BANNER_ID = 50L;

    private static final LocalDateTime EVENT_START = LocalDateTime.of(2026, 7, 1, 0, 0);
    private static final LocalDateTime EVENT_END = EVENT_START.plusDays(30);

    @Mock
    private BannerAdaptor eventBannerAdaptor;

    @Mock
    private AppEventAdaptor appEventAdaptor;

    private BannerService bannerService;

    @BeforeEach
    void setUp() {
        bannerService = new BannerService(eventBannerAdaptor, appEventAdaptor, new EventDisplayPeriodHelper());
    }

    private BannerCommand command(Long appEventId, LocalDateTime start, LocalDateTime end) {
        return new BannerCommand(appEventId, ContentTargetType.APP_EVENT, "여름 배너", "public/event/10/banner/7/img.png", start, end);
    }

    private AppEvent appEvent() {
        AppEvent e = AppEvent.builder()
                .name("앱 출시 이벤트").startAt(EVENT_START).endAt(EVENT_END).assigneeAdminId(ADMIN_ID)
                .build();
        ReflectionTestUtils.setField(e, "id", APP_EVENT_ID);
        return e;
    }

    private Banner existingBanner() {
        Banner b = Banner.builder()
                .appEvent(appEvent())
                .contentTargetType(ContentTargetType.APP_EVENT)
                .bannerTitle("기존").imageObjectKey("public/event/10/banner/7/old.png")
                .displayStartAt(EVENT_START.plusDays(1)).displayEndAt(EVENT_START.plusDays(2))
                .assigneeAdminId(ADMIN_ID)
                .build();
        ReflectionTestUtils.setField(b, "id", BANNER_ID);
        ReflectionTestUtils.setField(b, "status", BannerStatus.ACTIVE);
        return b;
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("배너 기간이 이벤트 기간 안이면 저장된다")
        void create_within_period() {
            given(appEventAdaptor.findById(APP_EVENT_ID)).willReturn(appEvent());
            given(eventBannerAdaptor.findOverlappingPeriods(any(), any(), eq(null))).willReturn(List.of());
            given(eventBannerAdaptor.save(any(Banner.class))).willAnswer(inv -> inv.getArgument(0));

            Banner saved = bannerService.create(
                    command(APP_EVENT_ID, EVENT_START.plusDays(3), EVENT_START.plusDays(5)), ADMIN_ID);

            assertThat(saved.getAppEvent().getId()).isEqualTo(APP_EVENT_ID);
            verify(eventBannerAdaptor).save(any(Banner.class));        }

        @Test
        @DisplayName("APP_EVENT 유형인데 appEventId 가 null 이면 예외 (소속 이벤트 필수)")
        void reject_app_event_type_without_event() {
            assertThatThrownBy(() -> bannerService.create(
                    command(null, EVENT_START.plusDays(3), EVENT_START.plusDays(5)), ADMIN_ID))
                    .isInstanceOf(BannerAppEventRequiredException.class);

            verify(appEventAdaptor, never()).findById(any());
            verify(eventBannerAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("배너 종료가 이벤트 종료를 넘으면 이벤트 종료로 clamp 되어 저장된다")
        void clamp_end_to_event_end() {
            given(appEventAdaptor.findById(APP_EVENT_ID)).willReturn(appEvent());
            given(eventBannerAdaptor.findOverlappingPeriods(any(), any(), eq(null))).willReturn(List.of());
            given(eventBannerAdaptor.save(any(Banner.class))).willAnswer(inv -> inv.getArgument(0));

            Banner saved = bannerService.create(
                    command(APP_EVENT_ID, EVENT_START.plusDays(3), EVENT_END.plusDays(1)), ADMIN_ID);

            assertThat(saved.getDisplayStartAt()).isEqualTo(EVENT_START.plusDays(3)); // 시작 그대로
            assertThat(saved.getDisplayEndAt()).isEqualTo(EVENT_END);                 // 종료만 clamp
        }

        @Test
        @DisplayName("배너 기간이 이벤트 기간과 전혀 겹치지 않으면 예외 (clamp 불가)")
        void reject_when_no_overlap() {
            given(appEventAdaptor.findById(APP_EVENT_ID)).willReturn(appEvent());

            assertThatThrownBy(() -> bannerService.create(
                    command(APP_EVENT_ID, EVENT_END.plusDays(1), EVENT_END.plusDays(2)), ADMIN_ID))
                    .isInstanceOf(BannerOutOfEventPeriodException.class);

            verify(eventBannerAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("동시 노출 피크가 이미 3이면 예외 (새 배너 포함 최대 3개)")
        void reject_when_exceeds_limit() {
            given(appEventAdaptor.findById(APP_EVENT_ID)).willReturn(appEvent());
            // [D1,D10] 3개가 새 배너 창(D3~D5)에서 동시 3 → 새 배너 포함 4라 거부
            given(eventBannerAdaptor.findOverlappingPeriods(any(), any(), eq(null))).willReturn(List.of(
                    new DisplayPeriod(EVENT_START.plusDays(1), EVENT_START.plusDays(10)),
                    new DisplayPeriod(EVENT_START.plusDays(1), EVENT_START.plusDays(10)),
                    new DisplayPeriod(EVENT_START.plusDays(1), EVENT_START.plusDays(10))));

            assertThatThrownBy(() -> bannerService.create(
                    command(APP_EVENT_ID, EVENT_START.plusDays(3), EVENT_START.plusDays(5)), ADMIN_ID))
                    .isInstanceOf(BannerOverlappingException.class);

            verify(eventBannerAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("동시 노출 피크가 2면(새 배너 포함 3) 저장된다")
        void allow_when_within_limit() {
            given(appEventAdaptor.findById(APP_EVENT_ID)).willReturn(appEvent());
            // [D1,D10] 2개가 동시 2 → 새 배너 포함 3이라 통과
            given(eventBannerAdaptor.findOverlappingPeriods(any(), any(), eq(null))).willReturn(List.of(
                    new DisplayPeriod(EVENT_START.plusDays(1), EVENT_START.plusDays(10)),
                    new DisplayPeriod(EVENT_START.plusDays(1), EVENT_START.plusDays(10))));
            given(eventBannerAdaptor.save(any(Banner.class))).willAnswer(inv -> inv.getArgument(0));

            Banner saved = bannerService.create(
                    command(APP_EVENT_ID, EVENT_START.plusDays(3), EVENT_START.plusDays(5)), ADMIN_ID);

            verify(eventBannerAdaptor).save(any(Banner.class));
            assertThat(saved.getAppEvent().getId()).isEqualTo(APP_EVENT_ID);
        }
    }

    @Nested
    @DisplayName("update - appEvent 불변, 기존 이벤트 기준")
    class Update {

        @Test
        @DisplayName("수정은 커맨드의 appEventId 를 무시하고 기존 배너의 이벤트 기간으로 clamp 한다")
        void update_uses_existing_app_event_ignoring_command() {
            given(eventBannerAdaptor.findById(BANNER_ID)).willReturn(existingBanner());
            given(eventBannerAdaptor.findOverlappingPeriods(any(), any(), eq(BANNER_ID))).willReturn(List.of());

            // 커맨드 appEventId(999)는 무시되고, 기존 배너의 이벤트(종료 EVENT_END)로 clamp 된다
            Banner updated = bannerService.update(BANNER_ID,
                    command(999L, EVENT_START.plusDays(4), EVENT_END.plusDays(2)));

            assertThat(updated.getAppEvent().getId()).isEqualTo(APP_EVENT_ID); // 이벤트 불변
            assertThat(updated.getDisplayEndAt()).isEqualTo(EVENT_END);        // 기존 이벤트 종료로 clamp
            verify(appEventAdaptor, never()).findById(any());                  // 커맨드 이벤트 로드 안 함
        }

        @Test
        @DisplayName("종료된 배너는 수정 시 겹침 검증을 건너뛴다")
        void update_skips_overlap_when_ended() {
            Banner ended = existingBanner();
            ReflectionTestUtils.setField(ended, "status", BannerStatus.ENDED);
            given(eventBannerAdaptor.findById(BANNER_ID)).willReturn(ended);

            bannerService.update(BANNER_ID, command(APP_EVENT_ID, EVENT_START.plusDays(4), EVENT_START.plusDays(6)));

            verify(eventBannerAdaptor, never()).findOverlappingPeriods(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("cancel 은 배너를 종료 상태로 만든다")
        void cancel_ends_banner() {
            Banner active = existingBanner();
            ReflectionTestUtils.setField(active, "status", BannerStatus.ACTIVE);
            given(eventBannerAdaptor.findById(BANNER_ID)).willReturn(active);

            Banner cancelled = bannerService.cancel(BANNER_ID);

            assertThat(cancelled.getStatus()).isEqualTo(BannerStatus.ENDED);
        }
    }
}
