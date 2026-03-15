package com.storix.domain.domains.plus.adaptor;

import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.plus.dto.CreateReaderBoardCommand;
import com.storix.domain.domains.plus.repository.ReaderBoardRepository;
import com.storix.domain.domains.feed.exception.InvalidBoardRequestException;
import com.storix.domain.domains.plus.exception.DuplicateBoardUploadException;
import com.storix.domain.domains.user.exception.auth.ForbiddenApproachException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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

    // 피드 작품 관련 게시글 조회
    public Slice<ReaderBoard> findAllReaderBoardListByWorksId(Long worksId, Pageable pageable) {
        if (worksId == null) {
            return null;
        }

        return readerBoardRepository.findAllReaderBoardByWorksId(worksId, pageable);
    }

    // 피드 게시글 단건 조회
    public ReaderBoard findReaderBoard(Long boardId) {
        Optional<ReaderBoard> readerBoard = readerBoardRepository.findById(boardId);
        if (readerBoard.isPresent()) {
            return readerBoard.get();
        } else {
            throw InvalidBoardRequestException.EXCEPTION;
        }
    }

    // 홈 오늘의 토픽룸 점수 갱신
    public int updateAllPopularityScoresRecentDays(LocalDateTime threshold) {
        return readerBoardRepository.updatePopularityScoresRecentDays(threshold);
    }

}
