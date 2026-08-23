package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventPageModalConfirmationAdaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[앱 이벤트] 이벤트 페이지 최초 노출 모달 확인 여부 - 조회 / 확인 처리")
class AppEventPageModalServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long APP_EVENT_ID = 50L;

    @Mock
    private AppEventPageModalConfirmationAdaptor appEventPageModalConfirmationAdaptor;

    @InjectMocks
    private AppEventPageModalService appEventPageModalService;

    @Test
    @DisplayName("확인한 적 없는 유저는 모달이 필요하다")
    void modal_required_when_not_confirmed() {
        given(appEventPageModalConfirmationAdaptor.isConfirmed(USER_ID, APP_EVENT_ID)).willReturn(false);

        assertThat(appEventPageModalService.isModalRequired(USER_ID, APP_EVENT_ID)).isTrue();
    }

    @Test
    @DisplayName("이미 확인한 유저는 모달이 필요 없다")
    void modal_not_required_when_confirmed() {
        given(appEventPageModalConfirmationAdaptor.isConfirmed(USER_ID, APP_EVENT_ID)).willReturn(true);

        assertThat(appEventPageModalService.isModalRequired(USER_ID, APP_EVENT_ID)).isFalse();
    }

    @Test
    @DisplayName("다른 이벤트의 확인 여부는 서로 영향을 주지 않는다")
    void modal_required_is_per_app_event() {
        Long otherAppEventId = 99L;
        given(appEventPageModalConfirmationAdaptor.isConfirmed(USER_ID, APP_EVENT_ID)).willReturn(true);
        given(appEventPageModalConfirmationAdaptor.isConfirmed(USER_ID, otherAppEventId)).willReturn(false);

        assertThat(appEventPageModalService.isModalRequired(USER_ID, APP_EVENT_ID)).isFalse();
        assertThat(appEventPageModalService.isModalRequired(USER_ID, otherAppEventId)).isTrue();
    }

    @Test
    @DisplayName("confirm 은 원자적 upsert 에 위임한다 (중복 호출에도 멱등)")
    void confirm_delegates_to_upsert() {
        appEventPageModalService.confirm(USER_ID, APP_EVENT_ID);

        verify(appEventPageModalConfirmationAdaptor).confirm(USER_ID, APP_EVENT_ID);
    }
}
