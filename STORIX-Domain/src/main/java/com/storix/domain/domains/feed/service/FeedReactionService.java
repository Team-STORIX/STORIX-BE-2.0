package com.storix.domain.domains.feed.service;

import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.feed.domain.ReaderBoardReply;
import com.storix.domain.domains.feed.dto.CreateFeedReplyCommand;
import com.storix.domain.domains.feed.dto.LikeToggleResponse;
import com.storix.domain.domains.feed.dto.ReaderBoardReplyResponse;
import com.storix.domain.domains.feed.dto.StandardReplyInfo;
import com.storix.domain.domains.feed.exception.ReplyDepthExceededException;
import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
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

    private final NotificationPublisher notificationPublisher;

    // 게시글 좋아요
    @Transactional
    public LikeToggleResponse toggleReaderBoardLike(Long userId, Long boardId) {

        // 게시글 작성자 조회
        Long boardOwnerUserId = readerFeedAdaptor.findBoardOwnerUserId(boardId);

        int isDeleted = readerFeedAdaptor.isBoardLikeDeleted(userId, boardId);
        if (isDeleted == 1) {
            return readerFeedAdaptor.deleteReaderBoardLike(boardId);
        }

        LikeToggleResponse response = readerFeedAdaptor.insertReaderBoardLike(userId, boardId);

        // 알림 발행
        publishFeedLikeNotification(userId, boardOwnerUserId, boardId);
        return response;
    }

    // 게시물 댓글 등록
    @Transactional
    public ReaderBoardReplyResponse uploadReaderBoardReply(Long userId, Long boardId, String comment) {

        ReaderBoard readerBoard = readerFeedAdaptor.findReaderBoardById(boardId);

        CreateFeedReplyCommand cmd =
                new CreateFeedReplyCommand(readerBoard, userId, comment, null);

        // 1. 댓글 정보
        StandardReplyInfo reply = readerFeedAdaptor.uploadReaderBoardReply(cmd);

        // 2. 프로필 정보
        StandardProfileInfo profile = userAdaptor.findStandardProfileInfoByUserId(userId);

        // 3. 알림 발행
        notificationPublisher.publishUnlessSelf(
                userId,
                NotificationEvent.commentOnFeed(readerBoard.getUserId(), boardId, profile.nickName(), comment)
        );

        return ReaderBoardReplyResponse.of(profile, reply);
    }

    // 답댓글 등록
    @Transactional
    public ReaderBoardReplyResponse uploadReaderBoardChildReply(Long userId, Long boardId, Long parentReplyId, String comment) {

        ReaderBoard readerBoard = readerFeedAdaptor.findReaderBoardById(boardId);
        ReaderBoardReply parentReply = readerFeedAdaptor.findReplyById(parentReplyId);

        // depth 1 제한 (답댓글에 대한 답댓글 불가)
        if (parentReply.getDepth() >= 1) {
            throw ReplyDepthExceededException.EXCEPTION;
        }

        CreateFeedReplyCommand cmd =
                new CreateFeedReplyCommand(readerBoard, userId, comment, parentReply);

        // 1. 댓글 정보
        StandardReplyInfo reply = readerFeedAdaptor.uploadReaderBoardChildReply(cmd);

        // 2. 프로필 정보
        StandardProfileInfo profile = userAdaptor.findStandardProfileInfoByUserId(userId);

        // 3. 알림
        notificationPublisher.publishUnlessSelf(
                userId,
                NotificationEvent.replyOnComment(parentReply.getUserId(), parentReplyId, boardId, profile.nickName(), comment)
        );

        return ReaderBoardReplyResponse.of(profile, reply);
    }

    // 게시글 댓글 좋아요
    @Transactional
    public LikeToggleResponse toggleReaderBoardReplyLike(Long userId, Long boardId, Long replyId) {

        // 댓글 작성자 조회
        Long replyOwnerUserId = readerFeedAdaptor.findReplyOwnerUserId(boardId, replyId);

        int isDeleted = readerFeedAdaptor.isReplyLikeDeleted(userId, replyId);
        if (isDeleted == 1) {
            return readerFeedAdaptor.deleteReaderBoardReplyLike(replyId);
        }

        LikeToggleResponse response = readerFeedAdaptor.insertReaderBoardReplyLike(userId, replyId);
        publishReplyLikeNotification(userId, replyOwnerUserId, boardId, replyId);
        return response;
    }

    /* ─────────── 알림 발행 헬퍼 ─────────── */

    private void publishFeedLikeNotification(Long actorUserId, Long boardOwnerUserId, Long boardId) {
        StandardProfileInfo actor = userAdaptor.findStandardProfileInfoByUserId(actorUserId);
        notificationPublisher.publishUnlessSelf(
                actorUserId,
                NotificationEvent.likeFeed(boardOwnerUserId, boardId, actor.nickName())
        );
    }

    private void publishReplyLikeNotification(Long actorUserId, Long replyOwnerUserId, Long boardId, Long replyId) {
        StandardProfileInfo actor = userAdaptor.findStandardProfileInfoByUserId(actorUserId);
        notificationPublisher.publishUnlessSelf(
                actorUserId,
                NotificationEvent.likeComment(replyOwnerUserId, replyId, boardId, actor.nickName())
        );
    }
}
