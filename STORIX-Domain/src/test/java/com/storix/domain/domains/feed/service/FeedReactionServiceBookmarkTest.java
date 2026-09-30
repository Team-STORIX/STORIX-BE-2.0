package com.storix.domain.domains.feed.service;

import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.feed.dto.BookmarkToggleResponse;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.works.application.helper.AdultWorksHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("[피드] 게시글 북마크 토글")
class FeedReactionServiceBookmarkTest {

    private static final Long USER_ID = 1L;
    private static final Long BOARD_ID = 10L;

    @Mock
    private UserAdaptor userAdaptor;

    @Mock
    private ReaderFeedAdaptor readerFeedAdaptor;

    @Mock
    private AdultWorksHelper adultWorksHelper;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private FeedReactionService feedReactionService;

    @BeforeEach
    void setUp() {
        ReaderBoard board = ReaderBoard.builder()
                .userId(2L)
                .isWorksSelected(false)
                .content("content")
                .build();
        given(readerFeedAdaptor.findReaderBoardById(BOARD_ID)).willReturn(board);
    }

    @Test
    @DisplayName("이미 북마크했으면 해제하고 삭제 여부는 확인하지 않는다")
    void removes_existing_bookmark() {
        given(readerFeedAdaptor.isBoardBookmarkDeleted(USER_ID, BOARD_ID)).willReturn(1);
        given(readerFeedAdaptor.deleteReaderBoardBookmark(BOARD_ID)).willReturn(new BookmarkToggleResponse(false, 0));

        BookmarkToggleResponse response = feedReactionService.toggleReaderBoardBookmark(USER_ID, BOARD_ID);

        assertThat(response.isBookmarked()).isFalse();
        verify(readerFeedAdaptor, never()).findActiveReaderBoardById(BOARD_ID);
        verify(readerFeedAdaptor, never()).insertReaderBoardBookmark(USER_ID, BOARD_ID);
    }

    @Test
    @DisplayName("북마크가 없으면 활성 게시글인지 확인하고 저장한다")
    void adds_bookmark_on_active_board() {
        given(readerFeedAdaptor.isBoardBookmarkDeleted(USER_ID, BOARD_ID)).willReturn(0);
        given(readerFeedAdaptor.insertReaderBoardBookmark(USER_ID, BOARD_ID)).willReturn(new BookmarkToggleResponse(true, 1));

        BookmarkToggleResponse response = feedReactionService.toggleReaderBoardBookmark(USER_ID, BOARD_ID);

        assertThat(response.isBookmarked()).isTrue();
        assertThat(response.bookmarkCount()).isEqualTo(1);
        verify(readerFeedAdaptor).findActiveReaderBoardById(BOARD_ID);
    }

    @Test
    @DisplayName("북마크는 비공개 활동이라 알림을 보내지 않는다")
    void does_not_notify() {
        given(readerFeedAdaptor.isBoardBookmarkDeleted(USER_ID, BOARD_ID)).willReturn(0);
        given(readerFeedAdaptor.insertReaderBoardBookmark(USER_ID, BOARD_ID)).willReturn(new BookmarkToggleResponse(true, 1));

        feedReactionService.toggleReaderBoardBookmark(USER_ID, BOARD_ID);

        verifyNoInteractions(notificationPublisher, userAdaptor);
    }
}
