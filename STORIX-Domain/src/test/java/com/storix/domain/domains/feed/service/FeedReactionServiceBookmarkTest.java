package com.storix.domain.domains.feed.service;

import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.feed.dto.BookmarkResponse;
import com.storix.domain.domains.feed.exception.InvalidBoardRequestException;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.works.application.helper.AdultWorksHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("[피드] 게시글 북마크")
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

    private ReaderBoard board() {
        return ReaderBoard.builder()
                .userId(2L)
                .isWorksSelected(false)
                .content("content")
                .build();
    }

    @Test
    @DisplayName("북마크는 활성 게시글인지 확인하고 저장한다")
    void bookmark_saves_on_active_board() {
        given(readerFeedAdaptor.findActiveReaderBoardById(BOARD_ID)).willReturn(board());
        given(readerFeedAdaptor.insertReaderBoardBookmark(USER_ID, BOARD_ID)).willReturn(new BookmarkResponse(true, 1));

        BookmarkResponse response = feedReactionService.bookmarkReaderBoard(USER_ID, BOARD_ID);

        assertThat(response.isBookmarked()).isTrue();
        assertThat(response.bookmarkCount()).isEqualTo(1);
        verify(readerFeedAdaptor, never()).deleteReaderBoardBookmark(anyLong(), anyLong());
    }

    @Test
    @DisplayName("삭제된 게시글은 새로 북마크할 수 없다")
    void bookmark_rejects_deleted_board() {
        given(readerFeedAdaptor.findActiveReaderBoardById(BOARD_ID)).willThrow(InvalidBoardRequestException.EXCEPTION);

        assertThatThrownBy(() -> feedReactionService.bookmarkReaderBoard(USER_ID, BOARD_ID))
                .isSameAs(InvalidBoardRequestException.EXCEPTION);
        verify(readerFeedAdaptor, never()).insertReaderBoardBookmark(anyLong(), anyLong());
    }

    @Test
    @DisplayName("해제는 삭제 여부와 상관없이 게시글이 있으면 지운다")
    void unbookmark_removes_without_active_check() {
        given(readerFeedAdaptor.findReaderBoardById(BOARD_ID)).willReturn(board());
        given(readerFeedAdaptor.deleteReaderBoardBookmark(USER_ID, BOARD_ID)).willReturn(new BookmarkResponse(false, 0));

        BookmarkResponse response = feedReactionService.unbookmarkReaderBoard(USER_ID, BOARD_ID);

        assertThat(response.isBookmarked()).isFalse();
        verify(readerFeedAdaptor, never()).findActiveReaderBoardById(anyLong());
        verify(readerFeedAdaptor, never()).insertReaderBoardBookmark(anyLong(), anyLong());
    }

    @Test
    @DisplayName("북마크는 비공개 활동이라 알림을 보내지 않는다")
    void does_not_notify() {
        given(readerFeedAdaptor.findActiveReaderBoardById(BOARD_ID)).willReturn(board());
        given(readerFeedAdaptor.insertReaderBoardBookmark(USER_ID, BOARD_ID)).willReturn(new BookmarkResponse(true, 1));

        feedReactionService.bookmarkReaderBoard(USER_ID, BOARD_ID);

        verifyNoInteractions(notificationPublisher, userAdaptor);
    }
}
