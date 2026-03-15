package com.storix.api.domain.feed.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.api.domain.feed.controller.dto.ReaderBoardReplyRequest;
import com.storix.domain.domains.feed.dto.LikeToggleResponse;
import com.storix.domain.domains.feed.dto.ReaderBoardReplyResponse;
import com.storix.domain.domains.feed.service.FeedReactionService;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class FeedReactionUseCase {

    private final FeedReactionService feedReactionService;

    // 독자 게시글 좋아요
    public CustomResponse<LikeToggleResponse> toggleReaderBoardLike(Long userId, Long boardId) {

        LikeToggleResponse result = feedReactionService.toggleReaderBoardLike(userId, boardId);
        return CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_LIKE_SUCCESS, result);
    }

    // 독자 댓글 작성
    public CustomResponse<ReaderBoardReplyResponse> writeReaderBoardReply(Long userId, Long boardId, ReaderBoardReplyRequest req) {

        ReaderBoardReplyResponse result = feedReactionService.uploadReaderBoardReply(userId, boardId, req.comment());
        return CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_REPLY_UPLOAD_SUCCESS, result);
    }

    // 독자 댓글 좋아요
    public CustomResponse<LikeToggleResponse> toggleReaderBoardReplyLike(Long userId, Long boardId, Long replyId) {

        LikeToggleResponse result = feedReactionService.toggleReaderBoardReplyLike(userId, boardId, replyId);
        return CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_REPLY_LIKE_SUCCESS, result);
    }

}
