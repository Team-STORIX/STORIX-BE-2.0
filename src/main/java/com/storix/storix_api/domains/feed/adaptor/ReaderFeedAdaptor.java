package com.storix.storix_api.domains.feed.adaptor;

import com.storix.storix_api.domains.feed.domain.ReaderBoardLike;
import com.storix.storix_api.domains.feed.domain.ReaderBoardReply;
import com.storix.storix_api.domains.feed.domain.ReaderBoardReplyLike;
import com.storix.storix_api.domains.feed.dto.CreateFeedReplyCommand;
import com.storix.storix_api.domains.feed.dto.LikeToggleResponse;
import com.storix.storix_api.domains.feed.dto.StandardReplyInfo;
import com.storix.storix_api.domains.feed.repository.ReaderBoardLikeRepository;
import com.storix.storix_api.domains.feed.repository.ReaderBoardReplyLikeRepository;
import com.storix.storix_api.domains.feed.repository.ReaderBoardReplyRepository;
import com.storix.storix_api.domains.plus.domain.ReaderBoard;
import com.storix.storix_api.domains.plus.dto.StandardReaderBoardInfo;
import com.storix.storix_api.domains.plus.repository.ReaderBoardRepository;
import com.storix.storix_api.global.apiPayload.exception.feed.BoardReplyNotFoundException;
import com.storix.storix_api.global.apiPayload.exception.feed.InvalidBoardRequestException;
import com.storix.storix_api.global.apiPayload.exception.user.ForbiddenApproachException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ReaderFeedAdaptor {

    private final ReaderBoardRepository readerBoardRepository;
    private final ReaderBoardLikeRepository readerBoardLikeRepository;
    private final ReaderBoardReplyRepository readerBoardReplyRepository;
    private final ReaderBoardReplyLikeRepository readerBoardReplyLikeRepository;

    // 게시물 존재 여부 확인
    public void checkReaderBoardExist(Long boardId) {
        if (!readerBoardRepository.existsById(boardId)) {
            throw InvalidBoardRequestException.EXCEPTION;
        }
    }

    // 게시글 확인
    public ReaderBoard findReaderBoardById(Long boardId) {
        Optional<ReaderBoard> readerBoard = readerBoardRepository.findById(boardId);
        if (readerBoard.isPresent()) {
            return readerBoard.get();
        } else {
            throw InvalidBoardRequestException.EXCEPTION;
        }
    }

    // 전체 게시글 확인
    public Slice<ReaderBoard> findAllByOrderByCreatedAtDesc(Pageable pageable) {
        return readerBoardRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // 리스트 좋아요 정보 확인
    public Set<Long> findLikedBoardIds(Long userId, List<Long> boardIds) {
        if (userId == null || boardIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(
                readerBoardLikeRepository.findLikedBoardIds(userId, boardIds)
        );
    }

    // 게시글 좋아요 여부 확인
    public boolean isBoardLiked(Long userId, Long boardId) {
        return readerBoardLikeRepository.existsByUserIdAndBoard_Id(userId, boardId);
    }


    // 게시글 좋아요 관련
    public int isBoardLikeDeleted(Long userId, Long boardId) {
        return readerBoardLikeRepository.deleteLike(userId, boardId);
    }

    public LikeToggleResponse deleteReaderBoardLike(Long boardId) {
        readerBoardRepository.decrementLikeCount(boardId);

        int likeCount = readerBoardRepository.findLikeCountById(boardId);
        return new LikeToggleResponse(false, likeCount);
    }

    public LikeToggleResponse insertReaderBoardLike(Long userId, Long boardId) {
        try {
            ReaderBoard boardRef = readerBoardRepository.getReferenceById(boardId);

            ReaderBoardLike like = ReaderBoardLike.of(boardRef, userId);
            readerBoardLikeRepository.saveAndFlush(like);

            readerBoardRepository.incrementLikeCount(boardId);

            int likeCount = readerBoardRepository.findLikeCountById(boardId);
            return new LikeToggleResponse(true, likeCount);

        } catch (DataIntegrityViolationException e) {

            int likeCount = readerBoardRepository.findLikeCountById(boardId);
            return new LikeToggleResponse(true, likeCount);
        }
    }

    // 댓글 생성
    public StandardReplyInfo uploadReaderBoardReply(CreateFeedReplyCommand cmd) {

        // 댓글 저장
        ReaderBoardReply reply = cmd.toEntity();
        readerBoardReplyRepository.save(reply);

        // 댓글 개수 증가
        readerBoardRepository.incrementReplyCount(cmd.readerBoard().getId());

        // 5) 응답 DTO 변환
        return StandardReplyInfo.from(reply);
    }

    // 댓글 존재 여부 확인
    public void checkReplyExist(Long boardId, Long replyId) {
        if (!readerBoardReplyRepository.existsByIdAndBoard_Id(replyId, boardId)) {
            throw BoardReplyNotFoundException.EXCEPTION;
        }
    }

    // 댓글 좋아요 관련
    public int isReplyLikeDeleted(Long userId, Long replyId) {
        return readerBoardReplyLikeRepository.deleteLike(userId, replyId);
    }

    public LikeToggleResponse deleteReaderBoardReplyLike(Long replyId) {
        readerBoardReplyRepository.decrementLikeCount(replyId);

        int likeCount = readerBoardReplyRepository.findLikeCountById(replyId);
        return new LikeToggleResponse(false, likeCount);
    }

    public LikeToggleResponse insertReaderBoardReplyLike(Long userId, Long replyId) {
        try {
            ReaderBoardReply replyRef = readerBoardReplyRepository.getReferenceById(replyId);

            ReaderBoardReplyLike like = ReaderBoardReplyLike.of(replyRef, userId);
            readerBoardReplyLikeRepository.saveAndFlush(like);

            readerBoardReplyRepository.incrementLikeCount(replyId);

            int likeCount = readerBoardReplyRepository.findLikeCountById(replyId);
            return new LikeToggleResponse(true, likeCount);

        } catch (DataIntegrityViolationException e) {

            int likeCount = readerBoardReplyRepository.findLikeCountById(replyId);
            return new LikeToggleResponse(true, likeCount);
        }
    }

    // 댓글 삭제
    public void deleteReaderBoardReply(Long userId, Long boardId, Long replyId) {

        Optional<ReaderBoardReply> readerBoardReply = readerBoardReplyRepository.findById(replyId);
        if (readerBoardReply.isPresent()) {
            if (readerBoardReply.get().getUserId().equals(userId)) {
                readerBoardReplyRepository.deleteById(replyId);
                readerBoardReplyRepository.flush();
                readerBoardRepository.decrementReplyCount(boardId);
            } else {
                throw ForbiddenApproachException.EXCEPTION;
            }
        } else {
            throw BoardReplyNotFoundException.EXCEPTION;
        }

    }

    // 게시물 - 댓글 정보 확인
    public Slice<ReaderBoardReply> findAllByBoardId(Long boardId, Pageable pageable) {
        return readerBoardReplyRepository.findAllByBoard_Id(boardId, pageable);
    }

    // 게시물 - 댓글 좋아요 여부 확인
    public Set<Long> findLikedReplyIds(Long userId, List<Long> replyIds) {
        if (userId == null || replyIds == null || replyIds.isEmpty()) {
            return Collections.emptySet();
        }

        return new HashSet<>(readerBoardReplyLikeRepository.findLikedReplyIds(userId, replyIds));
    }

    // 프로필 - 댓글 정보 확인
    public Slice<ReaderBoardReply> findAllByUserId(Long userId, Pageable pageable) {
        return readerBoardReplyRepository.findAllByUserId(userId, pageable);
    }

    // 프로필 - 좋아요한 게시글 정보 확인
    public Slice<ReaderBoard> findAllLikedReaderBoards(Long userId, Pageable pageable) {
        return readerBoardRepository.findAllLikedReaderBoards(userId, pageable);
    }

    // 오늘의 피드
    public List<StandardReaderBoardInfo> findTop3TrendingFeed(LocalDateTime threshold) {
        Pageable pageable = PageRequest.of(0, 3);

        return readerBoardRepository.findTop3TrendingFeed(threshold, pageable);
    }

    public List<StandardReaderBoardInfo> findSteadyTrendingFeedNotToday(List<Long> excludeIds, int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        if (excludeIds == null || excludeIds.isEmpty()) {
            return readerBoardRepository.findSteadyTrendingFeed(pageable);
        }
        return readerBoardRepository.findSteadyTrendingFeedNotToday(excludeIds, pageable);
    }

}
