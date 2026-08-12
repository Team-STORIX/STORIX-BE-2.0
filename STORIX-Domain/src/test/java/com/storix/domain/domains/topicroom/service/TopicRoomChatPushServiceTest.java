package com.storix.domain.domains.topicroom.service;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.topicroom.dto.RecentSender;
import com.storix.domain.domains.topicroom.dto.RecentSenderRow;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[토픽룸] 묶음 알림 최근 발신자")
class TopicRoomChatPushServiceTest {

    private static final Long ROOM_ID = 5L;
    private static final Long AFTER = 100L;
    private static final Long UP_TO = 148L;
    private static final int LIMIT = 3;

    @Mock
    private ChatAdaptor chatAdaptor;

    @Mock
    private UserAdaptor userAdaptor;

    @InjectMocks
    private TopicRoomChatPushService topicRoomChatPushService;

    @Nested
    @DisplayName("정렬과 개수")
    class Ordering {

        @Test
        @DisplayName("마지막 메시지가 최신인 발신자가 앞에 온다")
        void newest_sender_comes_first() {
            givenRows(
                    new RecentSenderRow(1L, 201L, 130L),
                    new RecentSenderRow(1L, 202L, 148L),
                    new RecentSenderRow(1L, 203L, 141L));
            givenProfiles(201L, 202L, 203L);

            List<RecentSender> senders = select(1L);

            assertThat(senders).extracting(RecentSender::userId).containsExactly(202L, 203L, 201L);
        }

        @Test
        @DisplayName("limit 을 넘으면 최신 순으로 잘린다")
        void caps_at_limit() {
            givenRows(
                    new RecentSenderRow(1L, 201L, 110L),
                    new RecentSenderRow(1L, 202L, 120L),
                    new RecentSenderRow(1L, 203L, 130L),
                    new RecentSenderRow(1L, 204L, 140L),
                    new RecentSenderRow(1L, 205L, 148L));
            givenProfiles(201L, 202L, 203L, 204L, 205L);

            List<RecentSender> senders = select(1L);

            assertThat(senders).extracting(RecentSender::userId).containsExactly(205L, 204L, 203L);
        }

        @Test
        @DisplayName("같은 발신자가 여러 건 보내도 한 번만 들어간다")
        void sender_appears_once() {
            givenRows(
                    new RecentSenderRow(1L, 201L, 148L),
                    new RecentSenderRow(1L, 202L, 120L));
            givenProfiles(201L, 202L);

            List<RecentSender> senders = select(1L);

            assertThat(senders).extracting(RecentSender::userId).containsExactly(201L, 202L);
        }
    }

    @Nested
    @DisplayName("수신자별 분리")
    class PerReceiver {

        @Test
        @DisplayName("수신자마다 자기 목록을 받는다")
        void each_receiver_gets_own_list() {
            givenRows(
                    new RecentSenderRow(1L, 201L, 148L),
                    new RecentSenderRow(1L, 202L, 120L),
                    new RecentSenderRow(2L, 202L, 120L));
            givenProfiles(201L, 202L);

            Map<Long, List<RecentSender>> result = selectAll(List.of(1L, 2L));

            assertThat(result.get(1L)).extracting(RecentSender::userId).containsExactly(201L, 202L);
            assertThat(result.get(2L)).extracting(RecentSender::userId).containsExactly(202L);
        }

        @Test
        @DisplayName("대상 행이 없으면 빈 맵이고 프로필 조회도 하지 않는다")
        void no_rows_skips_profile_lookup() {
            givenRows();

            Map<Long, List<RecentSender>> result = selectAll(List.of(1L));

            assertThat(result).isEmpty();
            verify(userAdaptor, never()).findStandardProfileInfoByUserIds(any());
        }
    }

    @Test
    @DisplayName("프로필을 못 찾은 발신자는 빠진다")
    void drops_sender_without_profile() {
        givenRows(
                new RecentSenderRow(1L, 201L, 148L),
                new RecentSenderRow(1L, 202L, 120L));
        given(userAdaptor.findStandardProfileInfoByUserIds(any()))
                .willReturn(Map.of(202L, profile(202L)));

        List<RecentSender> senders = select(1L);

        assertThat(senders).extracting(RecentSender::userId).containsExactly(202L);
    }

    @Test
    @DisplayName("프로필 이미지가 없으면 null 로 담긴다")
    void null_profile_image_is_kept() {
        givenRows(new RecentSenderRow(1L, 201L, 148L));
        given(userAdaptor.findStandardProfileInfoByUserIds(any()))
                .willReturn(Map.of(201L, new StandardProfileInfo(201L, null, "닉201")));

        assertThat(select(1L))
                .containsExactly(new RecentSender(201L, "닉201", null));
    }

    private List<RecentSender> select(Long receiverId) {
        return selectAll(List.of(receiverId)).get(receiverId);
    }

    private Map<Long, List<RecentSender>> selectAll(List<Long> receiverIds) {
        return topicRoomChatPushService.findRecentSendersByReceiver(
                ROOM_ID, receiverIds, AFTER, UP_TO, LIMIT);
    }

    private void givenRows(RecentSenderRow... rows) {
        given(chatAdaptor.findRecentSenderRows(anyLong(), any(), anyLong(), anyLong()))
                .willReturn(List.of(rows));
    }

    private void givenProfiles(Long... userIds) {
        given(userAdaptor.findStandardProfileInfoByUserIds(any()))
                .willReturn(java.util.Arrays.stream(userIds)
                        .collect(java.util.stream.Collectors.toMap(id -> id, this::profile)));
    }

    private StandardProfileInfo profile(Long userId) {
        return new StandardProfileInfo(userId, "https://cdn.storix.kr/" + userId + ".jpg", "닉" + userId);
    }
}
