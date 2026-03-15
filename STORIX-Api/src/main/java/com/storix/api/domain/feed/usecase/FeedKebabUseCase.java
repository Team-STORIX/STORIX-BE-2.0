package com.storix.api.domain.feed.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.api.domain.feed.controller.dto.FeedReportRequest;
import com.storix.domain.domains.feed.service.FeedKebabService;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class FeedKebabUseCase {

    private final FeedKebabService feedKebabService;

    // 내 게시물 삭제
    public CustomResponse<Void> deleteOwnBoard(Long userId, Long readerBoardId) {
        feedKebabService.deleteReaderBoard(userId, readerBoardId);
        return CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_DELETE_SUCCESS);
    }

    // 게시물 신고
    public CustomResponse<Void> reportFeed(Long userId, Long boardId, FeedReportRequest req) {
        feedKebabService.reportFeed(userId, boardId, req.reportedUserId());
        return CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_REPORT_SUCCESS);
    }

    // 내 댓글 삭제
    public CustomResponse<Void> deleteOwnReply(Long userId, Long boardId, Long replyId) {
        feedKebabService.deleteReaderBoardReply(userId, boardId, replyId);
        return CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_REPLY_DELETE_SUCCESS);
    }

    // 댓글 신고
    public CustomResponse<Void> reportFeedReply(Long userId, Long boardId, Long replyId, FeedReportRequest req) {
        feedKebabService.reportFeedReply(userId, boardId, replyId, req.reportedUserId());
        return CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_REPLY_REPORT_SUCCESS);
    }
}
