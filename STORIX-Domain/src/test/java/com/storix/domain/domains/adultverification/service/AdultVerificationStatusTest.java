package com.storix.domain.domains.adultverification.service;

import com.storix.common.property.PortOneProperties;
import com.storix.domain.domains.adultverification.adaptor.AdultVerificationAdaptor;
import com.storix.domain.domains.adultverification.domain.AdultVerification;
import com.storix.domain.domains.adultverification.domain.AdultVerificationState;
import com.storix.domain.domains.adultverification.dto.AdultVerificationStatusInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("[성인인증 상태 조회] 미인증 / 인증 완료 / 인증 만료")
class AdultVerificationStatusTest {

    private static final Long USER_ID = 9L;

    @Mock
    private AdultVerificationAdaptor adultVerificationAdaptor;
    @Mock
    private PortOneProperties portOneProperties;

    @InjectMocks
    private AdultVerificationService adultVerificationService;

    private AdultVerification verifiedOn(LocalDate day) {
        AdultVerification verification = AdultVerification.from(USER_ID, "identity-verification-test");
        verification.verify(day.atTime(14, 30), day.plusYears(1), "tx-1");
        return verification;
    }

    @Test
    @DisplayName("인증 이력이 없으면 NOT_VERIFIED 이고 진입할 수 있다")
    void noHistoryIsNotVerified() {
        when(adultVerificationAdaptor.findLatestSettled(anyLong())).thenReturn(Optional.empty());

        AdultVerificationStatusInfo info = adultVerificationService.getStatus(USER_ID);

        assertThat(info.state()).isEqualTo(AdultVerificationState.NOT_VERIFIED);
        assertThat(info.state().canVerify()).isTrue();
        assertThat(info.verifiedAt()).isNull();
        assertThat(info.expiresAt()).isNull();
    }

    @Test
    @DisplayName("유효한 인증이 있으면 VERIFIED 이고 진입할 수 없다")
    void activeVerificationIsVerified() {
        LocalDate verifiedOn = LocalDate.now().minusMonths(1);
        when(adultVerificationAdaptor.findLatestSettled(anyLong())).thenReturn(Optional.of(verifiedOn(verifiedOn)));

        AdultVerificationStatusInfo info = adultVerificationService.getStatus(USER_ID);

        assertThat(info.state()).isEqualTo(AdultVerificationState.VERIFIED);
        assertThat(info.state().canVerify()).isFalse();
        assertThat(info.expiresAt()).isEqualTo(verifiedOn.plusYears(1));
    }

    @Test
    @DisplayName("만료된 인증은 EXPIRED 이고, 미인증과 달리 지난 인증 시점을 보여준다")
    void overdueVerificationIsExpired() {
        LocalDate verifiedOn = LocalDate.now().minusYears(2);
        when(adultVerificationAdaptor.findLatestSettled(anyLong())).thenReturn(Optional.of(verifiedOn(verifiedOn)));

        AdultVerificationStatusInfo info = adultVerificationService.getStatus(USER_ID);

        assertThat(info.state()).isEqualTo(AdultVerificationState.EXPIRED);
        assertThat(info.state().canVerify()).isTrue();
        assertThat(info.verifiedAt()).isEqualTo(verifiedOn.atTime(14, 30));
    }

    @Test
    @DisplayName("만료일 당일은 아직 VERIFIED, 다음 날부터 EXPIRED")
    void expiryBoundaryIsInclusive() {
        LocalDate today = LocalDate.now();

        when(adultVerificationAdaptor.findLatestSettled(anyLong()))
                .thenReturn(Optional.of(verifiedOn(today.minusYears(1))));
        assertThat(adultVerificationService.getStatus(USER_ID).state())
                .isEqualTo(AdultVerificationState.VERIFIED);

        when(adultVerificationAdaptor.findLatestSettled(anyLong()))
                .thenReturn(Optional.of(verifiedOn(today.minusYears(1).minusDays(1))));
        assertThat(adultVerificationService.getStatus(USER_ID).state())
                .isEqualTo(AdultVerificationState.EXPIRED);
    }
}
