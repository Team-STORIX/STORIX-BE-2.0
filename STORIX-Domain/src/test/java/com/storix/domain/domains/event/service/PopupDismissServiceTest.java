package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.PopupDismissAdaptor;
import com.storix.domain.domains.event.domain.PopupExposurePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[이벤트 팝업] 오늘 다시 안 보기 / 다시 보지 않기 - 원자적 upsert / 조회")
class PopupDismissServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long POPUP_ID = 50L;
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 15);

    @Mock
    private PopupDismissAdaptor popupDismissAdaptor;

    @InjectMocks
    private PopupDismissService popupDismissService;

    @Test
    @DisplayName("dismiss 는 원자적 upsert 에 위임한다 (중복 탭/동시 요청에도 멱등)")
    void dismiss_delegates_to_upsert() {
        popupDismissService.dismissForToday(USER_ID, POPUP_ID, TODAY);

        verify(popupDismissAdaptor).upsertForToday(USER_ID, POPUP_ID, TODAY);
    }

    @Test
    @DisplayName("다시 보지 않기는 영구 upsert 에 위임한다")
    void dismiss_forever_delegates_to_never_show_upsert() {
        popupDismissService.dismissForever(USER_ID, POPUP_ID, TODAY);

        verify(popupDismissAdaptor).upsertNeverShow(USER_ID, POPUP_ID, TODAY);
    }

    @Test
    @DisplayName("ONCE_PER_DAY - 오늘 dismiss 또는 영구 dismiss 했으면 숨김")
    void suppressed_once_per_day() {
        given(popupDismissAdaptor.isSuppressedOn(USER_ID, POPUP_ID, TODAY)).willReturn(true);

        assertThat(popupDismissService.isSuppressed(USER_ID, POPUP_ID, PopupExposurePolicy.ONCE_PER_DAY, TODAY)).isTrue();
    }

    @Test
    @DisplayName("ALWAYS_DURING_PERIOD - 영구 dismiss 한 유저만 숨김, 그 외 항상 노출")
    void suppressed_when_always_only_if_permanent() {
        given(popupDismissAdaptor.isPermanentlyDismissed(USER_ID, POPUP_ID)).willReturn(false);
        assertThat(popupDismissService.isSuppressed(USER_ID, POPUP_ID, PopupExposurePolicy.ALWAYS_DURING_PERIOD, TODAY)).isFalse();

        given(popupDismissAdaptor.isPermanentlyDismissed(USER_ID, POPUP_ID)).willReturn(true);
        assertThat(popupDismissService.isSuppressed(USER_ID, POPUP_ID, PopupExposurePolicy.ALWAYS_DURING_PERIOD, TODAY)).isTrue();
    }
}
