package com.storix.domain.domains.plus.adaptor;

import com.storix.domain.domains.feed.repository.ReaderBoardLikeRepository;
import com.storix.domain.domains.feed.repository.ReaderBoardReplyLikeRepository;
import com.storix.domain.domains.feed.repository.ReaderBoardReplyRepository;
import com.storix.domain.domains.image.publisher.S3CleanupPublisher;
import com.storix.domain.domains.plus.domain.BoardImage;
import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.plus.dto.BoardHardDeleteResult;
import com.storix.domain.domains.plus.dto.CreateReaderBoardCommand;
import com.storix.domain.domains.plus.repository.ReaderBoardRepository;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.feed.exception.InvalidBoardRequestException;
import com.storix.domain.domains.plus.exception.DuplicateBoardUploadException;
import com.storix.domain.domains.user.exception.auth.ForbiddenApproachException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BoardAdaptor {

    private final ReaderBoardRepository readerBoardRepository;
    private final BoardImageAdaptor boardImageAdaptor;
    private final ReaderBoardLikeRepository readerBoardLikeRepository;
    private final ReaderBoardReplyRepository readerBoardReplyRepository;
    private final ReaderBoardReplyLikeRepository readerBoardReplyLikeRepository;
    private final S3CleanupPublisher s3CleanupPublisher;

    /**
     * 독자
     * */
    // 독자 게시글 생성
    public ReaderBoard saveReaderBoard(CreateReaderBoardCommand cmd) {
        try {
            ReaderBoard readerBoard = cmd.toEntity();
            return readerBoardRepository.save(readerBoard);
        } catch (DataIntegrityViolationException e) {
            throw DuplicateBoardUploadException.EXCEPTION;
        }
    }

    // 독자 내 게시글 조회
    public Slice<ReaderBoard> findAllReaderBoardList(Long userId, Pageable pageable) {
        return readerBoardRepository.findAllReaderBoardByUserId(userId, pageable);
    }

    // 독자 게시글 삭제
    public void deleteSingleReaderBoard(Long userId, Long boardId) {
        ReaderBoard board = readerBoardRepository.findById(boardId)
                .orElseThrow(() -> InvalidBoardRequestException.EXCEPTION);

        if (!board.getUserId().equals(userId)) {
            throw ForbiddenApproachException.EXCEPTION;
        }

        List<String> imageObjectKeys = board.getImages().stream()
                .map(BoardImage::getImageObjectKey)
                .toList();

        readerBoardRepository.deleteById(boardId);
        s3CleanupPublisher.publish(imageObjectKeys);
    }

    // 관리자 게시글 강제 삭제 — 이미 삭제된 경우 null 반환 (idempotent)
    public Long adminDeleteReaderBoard(Long boardId) {
        ReaderBoard board = readerBoardRepository.findById(boardId)
                .orElseThrow(() -> InvalidBoardRequestException.EXCEPTION);
        return board.softDeleteByAdmin() ? board.getUserId() : null;
    }

    // 피드 작품 관련 게시글 조회
    public Slice<ReaderBoard> findAllReaderBoardListByWorksId(Long worksId, Pageable pageable) {
        if (worksId == null) {
            return null;
        }

        return readerBoardRepository.findAllReaderBoardByWorksId(worksId, pageable);
    }

    public Slice<ReaderBoard> findAllReaderBoardListByWorksIdExcludingBlocked(Long worksId, List<Long> blockedIds, Pageable pageable) {
        if (worksId == null) {
            return null;
        }
        if (blockedIds.isEmpty()) {
            return readerBoardRepository.findAllReaderBoardByWorksId(worksId, pageable);
        }
        return readerBoardRepository.findAllReaderBoardByWorksIdExcludingBlocked(worksId, blockedIds, pageable);
    }

    // 피드 게시글 단건 조회 (삭제된 게시글 차단)
    public ReaderBoard findReaderBoard(Long boardId) {
        return readerBoardRepository.findByIdAndDeletedFalse(boardId)
                .orElseThrow(() -> InvalidBoardRequestException.EXCEPTION);
    }

    // 홈 오늘의 토픽룸 점수 갱신
    public int updateAllPopularityScoresRecentDays(LocalDateTime threshold) {
        return readerBoardRepository.updatePopularityScoresRecentDays(threshold);
    }

    // 하드 삭제 대상 정리 : 댓글 좋아요 → 댓글 → 좋아요 → 이미지
    public BoardHardDeleteResult hardDeleteBoardsBefore(LocalDateTime cutoff) {
        int boardCount = 0;
        int imageCount = 0;

        while (true) {
            List<Long> boardIds = readerBoardRepository.findIdsForHardDelete(
                    cutoff, PageRequest.of(0, STORIXStatic.HARD_DELETE_CHUNK_SIZE));
            if (boardIds.isEmpty()) {
                break;
            }

            List<String> imageObjectKeys = boardImageAdaptor.findObjectKeysByBoardIds(boardIds);

            readerBoardReplyLikeRepository.hardDeleteByBoardIds(boardIds);
            readerBoardReplyRepository.detachChildRepliesByBoardIds(boardIds);
            readerBoardReplyRepository.hardDeleteByBoardIds(boardIds);
            readerBoardLikeRepository.hardDeleteByBoardIds(boardIds);
            boardImageAdaptor.hardDeleteByBoardIds(boardIds);
            boardCount += readerBoardRepository.hardDeleteByIds(boardIds);

            imageCount += imageObjectKeys.size();
            s3CleanupPublisher.publish(imageObjectKeys);
        }

        return new BoardHardDeleteResult(boardCount, imageCount);
    }

}
