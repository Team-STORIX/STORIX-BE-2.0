package com.storix.domain.domains.profile.service;

import com.storix.domain.domains.profile.exception.SelfProfileRequestException;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserBlockAdaptor;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.exception.block.BlockedUserContentException;
import com.storix.domain.domains.user.exception.me.UnknownUserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("[프로필] 타 사용자 프로필 접근")
class OtherProfileServiceTest {

    private static final Long VIEWER_ID = 1L;
    private static final Long TARGET_ID = 2L;

    @Mock
    private UserAdaptor userAdaptor;

    @Mock
    private UserBlockAdaptor userBlockAdaptor;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private OtherProfileService otherProfileService;

    private User targetWith(AccountState state) {
        User target = mock(User.class);
        given(target.getAccountState()).willReturn(state);
        given(userAdaptor.findUserById(TARGET_ID)).willReturn(target);
        return target;
    }

    @Nested
    @DisplayName("조회 가능 여부")
    class Viewable {

        @Test
        @DisplayName("본인 id 로는 조회할 수 없다")
        void self_request_rejected() {
            assertThatThrownBy(() -> otherProfileService.getViewableUser(VIEWER_ID, VIEWER_ID))
                    .isSameAs(SelfProfileRequestException.EXCEPTION);
            verifyNoInteractions(userAdaptor, userBlockAdaptor);
        }

        @Test
        @DisplayName("없는 유저면 404")
        void unknown_user() {
            given(userAdaptor.findUserById(TARGET_ID)).willThrow(UnknownUserException.EXCEPTION);

            assertThatThrownBy(() -> otherProfileService.getViewableUser(VIEWER_ID, TARGET_ID))
                    .isSameAs(UnknownUserException.EXCEPTION);
        }

        @Test
        @DisplayName("탈퇴 유저는 없는 유저로 취급한다")
        void withdrawn_user() {
            targetWith(AccountState.DELETED);

            assertThatThrownBy(() -> otherProfileService.getViewableUser(VIEWER_ID, TARGET_ID))
                    .isSameAs(UnknownUserException.EXCEPTION);
        }

        @Test
        @DisplayName("정지 유저는 없는 유저로 취급한다")
        void suspended_user() {
            targetWith(AccountState.SUSPENDED);

            assertThatThrownBy(() -> otherProfileService.getViewableUser(VIEWER_ID, TARGET_ID))
                    .isSameAs(UnknownUserException.EXCEPTION);
        }

        @Test
        @DisplayName("내가 차단한 유저면 403")
        void viewer_blocked_target() {
            targetWith(AccountState.NORMAL);
            given(userBlockAdaptor.isBlocked(VIEWER_ID, TARGET_ID)).willReturn(true);

            assertThatThrownBy(() -> otherProfileService.getViewableUser(VIEWER_ID, TARGET_ID))
                    .isSameAs(BlockedUserContentException.EXCEPTION);
        }

        @Test
        @DisplayName("나를 차단한 유저의 프로필은 볼 수 있고, 반대 방향 차단은 조회하지 않는다")
        void blocked_viewer_can_still_view() {
            User target = targetWith(AccountState.NORMAL);
            given(userBlockAdaptor.isBlocked(VIEWER_ID, TARGET_ID)).willReturn(false);

            assertThat(otherProfileService.getViewableUser(VIEWER_ID, TARGET_ID)).isSameAs(target);
            verify(userBlockAdaptor, never()).isBlocked(TARGET_ID, VIEWER_ID);
        }
    }

    @Nested
    @DisplayName("서재 공개 여부")
    class LibraryVisibility {

        @Test
        @DisplayName("공개 서재")
        void public_library() {
            User target = targetWith(AccountState.NORMAL);
            given(target.isLibraryPublic()).willReturn(true);

            assertThat(otherProfileService.isLibraryPublic(VIEWER_ID, TARGET_ID)).isTrue();
        }

        @Test
        @DisplayName("비공개 서재")
        void private_library() {
            User target = targetWith(AccountState.NORMAL);
            given(target.isLibraryPublic()).willReturn(false);

            assertThat(otherProfileService.isLibraryPublic(VIEWER_ID, TARGET_ID)).isFalse();
        }
    }
}
