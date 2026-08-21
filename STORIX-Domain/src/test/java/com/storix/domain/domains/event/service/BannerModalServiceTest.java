package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.BannerModalConfirmationAdaptor;
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
@DisplayName("[이벤트 배너] 최초 노출 모달 확인 여부 - 조회 / 확인 처리")
class BannerModalServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long BANNER_ID = 50L;

    @Mock
    private BannerModalConfirmationAdaptor bannerModalConfirmationAdaptor;

    @InjectMocks
    private BannerModalService bannerModalService;

    @Test
    @DisplayName("확인한 적 없는 유저는 모달이 필요하다")
    void modal_required_when_not_confirmed() {
        given(bannerModalConfirmationAdaptor.isConfirmed(USER_ID, BANNER_ID)).willReturn(false);

        assertThat(bannerModalService.isModalRequired(USER_ID, BANNER_ID)).isTrue();
    }

    @Test
    @DisplayName("이미 확인한 유저는 모달이 필요 없다")
    void modal_not_required_when_confirmed() {
        given(bannerModalConfirmationAdaptor.isConfirmed(USER_ID, BANNER_ID)).willReturn(true);

        assertThat(bannerModalService.isModalRequired(USER_ID, BANNER_ID)).isFalse();
    }

    @Test
    @DisplayName("다른 배너의 확인 여부는 서로 영향을 주지 않는다")
    void modal_required_is_per_banner() {
        Long otherBannerId = 99L;
        given(bannerModalConfirmationAdaptor.isConfirmed(USER_ID, BANNER_ID)).willReturn(true);
        given(bannerModalConfirmationAdaptor.isConfirmed(USER_ID, otherBannerId)).willReturn(false);

        assertThat(bannerModalService.isModalRequired(USER_ID, BANNER_ID)).isFalse();
        assertThat(bannerModalService.isModalRequired(USER_ID, otherBannerId)).isTrue();
    }

    @Test
    @DisplayName("confirm 은 원자적 upsert 에 위임한다 (중복 호출에도 멱등)")
    void confirm_delegates_to_upsert() {
        bannerModalService.confirm(USER_ID, BANNER_ID);

        verify(bannerModalConfirmationAdaptor).confirm(USER_ID, BANNER_ID);
    }
}
