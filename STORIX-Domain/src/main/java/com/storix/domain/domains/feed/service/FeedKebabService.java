package com.storix.domain.domains.feed.service;

import com.storix.domain.domains.feed.adaptor.FeedReportAdaptor;
import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.feed.dto.CreateFeedReportCommand;
import com.storix.domain.domains.feed.exception.DuplicateFeedReplyReportException;
import com.storix.domain.domains.feed.exception.DuplicateFeedReportException;
import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.report.adaptor.ReportCaseAdaptor;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.TargetContentType;
import com.storix.domain.domains.topicroom.exception.SelfReportException;
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
    private final ReportCaseAdaptor reportCaseAdaptor;
    private final NotificationPublisher notificationPublisher;

    // 내 게시물 삭제
    @Transactional
    public void deleteReaderBoard(Long userId, Long readerBoardId) {
        boardAdaptor.deleteSingleReaderBoard(userId, readerBoardId);
        libraryAdaptor.decrementBoardCount(userId);
    }

    // 게시물 신고
    @Transactional
    public void reportFeed(Long userId, Long boardId, Long reportedUserId) {

        readerFeedAdaptor.checkReaderBoardExist(boardId);

        if (userId.equals(reportedUserId)) {
            throw SelfReportException.EXCEPTION;
        }

        if (feedReportAdaptor.hasAlreadyReported(userId, boardId)) {
            throw DuplicateFeedReportException.EXCEPTION;
        }

        ReportCase reportCase = reportCaseAdaptor.findOrCreate(TargetContentType.FEED, boardId, reportedUserId);

        CreateFeedReportCommand cmd = new CreateFeedReportCommand(
                userId,
                reportedUserId,
                boardId,
                reportCase.getId()
        );

        feedReportAdaptor.saveReport(cmd);
        notificationPublisher.publish(NotificationEvent.reportReceived(userId));
    }

    // 내 댓글 삭제
    @Transactional
    public void deleteReaderBoardReply(Long userId, Long boardId, Long replyId) {
        readerFeedAdaptor.deleteReaderBoardReply(userId, boardId, replyId);
    }

    // 댓글 신고
    @Transactional
    public void reportFeedReply(Long userId, Long boardId, Long replyId, Long reportedUserId) {

        readerFeedAdaptor.checkReplyExist(boardId, replyId);

        if (userId.equals(reportedUserId)) {
            throw SelfReportException.EXCEPTION;
        }

        if (feedReportAdaptor.hasAlreadyReplyReported(userId, replyId)) {
            throw DuplicateFeedReplyReportException.EXCEPTION;
        }

        ReportCase reportCase = reportCaseAdaptor.findOrCreate(TargetContentType.FEED_REPLY, replyId, reportedUserId);

        CreateFeedReportCommand cmd = new CreateFeedReportCommand(
                userId,
                reportedUserId,
                replyId,
                reportCase.getId()
        );

        feedReportAdaptor.saveReplyReport(cmd);
        notificationPublisher.publish(NotificationEvent.reportReceived(userId));
    }
}
