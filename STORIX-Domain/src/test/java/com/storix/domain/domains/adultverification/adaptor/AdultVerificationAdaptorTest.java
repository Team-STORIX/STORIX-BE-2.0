package com.storix.domain.domains.adultverification.adaptor;

import com.storix.domain.domains.adultverification.domain.AdultVerificationStatus;
import com.storix.domain.domains.adultverification.repository.AdultVerificationRepository;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[성인 콘텐츠] 열람 가능 여부 - 관리자만 성인인증 없이 허용")
class AdultVerificationAdaptorTest {

    private static final Long USER_ID = 1L;

    @Mock
    private AdultVerificationRepository adultVerificationRepository;
    @InjectMocks
    private AdultVerificationAdaptor adultVerificationAdaptor;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(Long userId, Role role) {
        AuthUserDetails details = new AuthUserDetails(userId, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, "user", details.getAuthorities()));
    }

    @Test
    @DisplayName("관리자는 성인인증 없이 볼 수 있고 인증 이력을 조회하지 않는다")
    void adminCanViewWithoutVerification() {
        loginAs(USER_ID, Role.ADMIN);

        assertThat(adultVerificationAdaptor.excludeAdultFor(USER_ID)).isFalse();
        verify(adultVerificationRepository, never()).findLatestVerifiedAtByUserId(anyLong(), any());
    }

    @Test
    @DisplayName("테스터는 성인인증이 없으면 볼 수 없다")
    void testerWithoutVerificationCannotView() {
        loginAs(USER_ID, Role.TESTER);
        given(adultVerificationRepository.findLatestVerifiedAtByUserId(USER_ID, AdultVerificationStatus.VERIFIED)).willReturn(null);

        assertThat(adultVerificationAdaptor.excludeAdultFor(USER_ID)).isTrue();
    }

    @Test
    @DisplayName("관리자 토큰이어도 다른 유저 기준 판정에는 적용하지 않는다")
    void adminTokenDoesNotApplyToOtherUser() {
        loginAs(2L, Role.ADMIN);
        given(adultVerificationRepository.findLatestVerifiedAtByUserId(USER_ID, AdultVerificationStatus.VERIFIED)).willReturn(null);

        assertThat(adultVerificationAdaptor.excludeAdultFor(USER_ID)).isTrue();
    }

    @Test
    @DisplayName("관리자가 아니어도 유효한 성인인증이 있으면 볼 수 있다")
    void readerWithValidVerificationCanView() {
        loginAs(USER_ID, Role.READER);
        given(adultVerificationRepository.findLatestVerifiedAtByUserId(USER_ID, AdultVerificationStatus.VERIFIED))
                .willReturn(LocalDateTime.now().minusMonths(1));

        assertThat(adultVerificationAdaptor.excludeAdultFor(USER_ID)).isFalse();
    }

    @Test
    @DisplayName("비로그인은 볼 수 없다")
    void anonymousCannotView() {
        assertThat(adultVerificationAdaptor.excludeAdultFor(null)).isTrue();
    }
}
