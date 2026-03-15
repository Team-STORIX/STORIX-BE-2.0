package com.storix.storix_api.domains.feed.service;

import com.storix.storix_api.domains.feed.adaptor.FeedReportAdaptor;
import com.storix.storix_api.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.storix_api.domains.feed.dto.CreateFeedReportCommand;
import com.storix.storix_api.domains.feed.controller.dto.FeedReportRequest;
import com.storix.storix_api.domains.library.adaptor.LibraryAdaptor;
import com.storix.storix_api.domains.plus.adaptor.BoardAdaptor;
import com.storix.storix_api.global.apiPayload.exception.topicRoom.SelfReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedKebabService {

    private final BoardAdaptor boardAdaptor;
    private final LibraryAdaptor libraryAdaptor;
    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final FeedReportAdaptor feedReportAdaptor;

    // 내 게시물 삭제
    @Transactional
    public void deleteReaderBoard(Long userId, Long readerBoardId) {
        boardAdaptor.deleteSingleReaderBoard(userId, readerBoardId);
        libraryAdaptor.decrementBoardCount(userId);
    }

    // 게시물 신고
    @Transactional
    public void reportFeed (Long userId, Long boardId, FeedReportRequest req) {

        readerFeedAdaptor.checkReaderBoardExist(boardId);

        if (userId.equals(req.reportedUserId())) {
            throw SelfReportException.EXCEPTION;
        }

        CreateFeedReportCommand cmd = new CreateFeedReportCommand(
                userId,
                req.reportedUserId(),
                boardId
        );

        feedReportAdaptor.saveReport(cmd);
    }

    // 내 댓글 삭제
    @Transactional
    public void deleteReaderBoardReply(Long userId, Long boardId, Long replyId) {
        readerFeedAdaptor.deleteReaderBoardReply(userId, boardId, replyId);
    }

    // 댓글 신고
    @Transactional
    public void reportFeedReply (Long userId, Long boardId, Long replyId, FeedReportRequest req) {

        readerFeedAdaptor.checkReplyExist(boardId, replyId);

        if (userId.equals(req.reportedUserId())) {
            throw SelfReportException.EXCEPTION;
        }

        CreateFeedReportCommand cmd = new CreateFeedReportCommand(
                userId,
                req.reportedUserId(),
                replyId
        );

        feedReportAdaptor.saveReplyReport(cmd);
    }
}
