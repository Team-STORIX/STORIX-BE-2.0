package com.storix.domain.domains.feed.service;

import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.feed.dto.CreateFeedReplyCommand;
import com.storix.domain.domains.feed.dto.LikeToggleResponse;
import com.storix.domain.domains.feed.dto.ReaderBoardReplyResponse;
import com.storix.domain.domains.feed.dto.StandardReplyInfo;
import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedReactionService {

    private final UserAdaptor userAdaptor;
    private final ReaderFeedAdaptor readerFeedAdaptor;

    // 게시글 좋아요
    @Transactional
    public LikeToggleResponse toggleReaderBoardLike(Long userId, Long boardId) {

        readerFeedAdaptor.checkReaderBoardExist(boardId);

        int isDeleted = readerFeedAdaptor.isBoardLikeDeleted(userId, boardId);
        if (isDeleted == 1) {
            return readerFeedAdaptor.deleteReaderBoardLike(boardId);
        } else {
            return readerFeedAdaptor.insertReaderBoardLike(userId, boardId);
        }

    }

    // 게시물 댓글 등록
    @Transactional
    public ReaderBoardReplyResponse uploadReaderBoardReply(Long userId, Long boardId, String comment) {

        ReaderBoard readerBoard = readerFeedAdaptor.findReaderBoardById(boardId);

        CreateFeedReplyCommand cmd =
                new CreateFeedReplyCommand(readerBoard, userId, comment);

        // 1) 댓글 정보
        StandardReplyInfo reply = readerFeedAdaptor.uploadReaderBoardReply(cmd);

        // 2) 프로필 정보
        StandardProfileInfo profile = userAdaptor.findStandardProfileInfoByUserId(userId);

        return ReaderBoardReplyResponse.of(profile, reply);
    }

    // 게시글 댓글 좋아요
    @Transactional
    public LikeToggleResponse toggleReaderBoardReplyLike(Long userId, Long boardId, Long replyId) {

        readerFeedAdaptor.checkReplyExist(boardId, replyId);

        int isDeleted = readerFeedAdaptor.isReplyLikeDeleted(userId, replyId);
        if (isDeleted == 1) {
            return readerFeedAdaptor.deleteReaderBoardReplyLike(replyId);
        } else {
            return readerFeedAdaptor.insertReaderBoardReplyLike(userId, replyId);
        }

    }



}
