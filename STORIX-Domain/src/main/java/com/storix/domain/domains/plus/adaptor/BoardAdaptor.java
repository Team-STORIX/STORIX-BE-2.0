package com.storix.domain.domains.plus.adaptor;

import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.plus.dto.CreateReaderBoardCommand;
import com.storix.domain.domains.plus.repository.ReaderBoardRepository;
import com.storix.domain.domains.feed.exception.InvalidBoardRequestException;
import com.storix.domain.domains.plus.exception.DuplicateBoardUploadException;
import com.storix.domain.domains.user.dto.AdminUserContentItemResponse;
import com.storix.domain.domains.user.exception.auth.ForbiddenApproachException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BoardAdaptor {

    private final ReaderBoardRepository readerBoardRepository;

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

        Optional<ReaderBoard> readerBoard = readerBoardRepository.findById(boardId);
        if (readerBoard.isPresent()) {
            if (readerBoard.get().getUserId().equals(userId)) {
                readerBoardRepository.deleteById(boardId);
                readerBoardRepository.flush();
            } else {
                throw ForbiddenApproachException.EXCEPTION;
            }
        } else {
            throw InvalidBoardRequestException.EXCEPTION;
        }

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

    public int hardDeleteBefore(LocalDateTime cutoff) {
        return readerBoardRepository.hardDeleteBefore(cutoff);
    }

    public long countActiveBoardsByUserId(Long userId) {
        return readerBoardRepository.countByUserIdAndDeletedFalse(userId);
    }

    public Page<AdminUserContentItemResponse> findAdminBoardContentsByUserId(Long userId, Pageable pageable) {
        return readerBoardRepository.findAdminBoardContentsByUserId(userId, pageable);
    }

    public List<AdminUserContentItemResponse> findAdminBoardContentsByIds(List<Long> ids) {
        return readerBoardRepository.findAdminBoardContentsByIds(ids);
    }

}
