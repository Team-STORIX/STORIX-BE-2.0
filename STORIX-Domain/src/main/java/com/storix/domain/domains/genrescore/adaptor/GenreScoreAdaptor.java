package com.storix.domain.domains.genrescore.adaptor;

import com.storix.domain.domains.genrescore.domain.UserGenreRawScore;
import com.storix.domain.domains.genrescore.domain.UserGenreScoreLog;
import com.storix.domain.domains.genrescore.dto.RecentGenreScore;
import com.storix.domain.domains.genrescore.dto.UnprocessedLogRow;
import com.storix.domain.domains.genrescore.repository.UserGenreRawScoreRepository;
import com.storix.domain.domains.genrescore.repository.UserGenreScoreLogRepository;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// 장르 점수(raw_score / score_log) 데이터 접근 어댑터
@Component
@RequiredArgsConstructor
public class GenreScoreAdaptor {

    private final UserGenreRawScoreRepository rawScoreRepository;
    private final UserGenreScoreLogRepository logRepository;

    /** 조회 작업 관련 메서드 */
    // 유저의 장르별 raw_score 전체 조회
    public List<UserGenreRawScore> findRawScoresByUserId(Long userId) {
        return rawScoreRepository.findAllByIdUserId(userId);
    }

    // 여러 유저의 장르별 raw_score 조회
    public List<UserGenreRawScore> findRawScoresByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return rawScoreRepository.findAllByIdUserIdIn(userIds);
    }

    // 미처리 로그 청크 조회
    public List<UnprocessedLogRow> findUnprocessedLogChunk(Pageable pageable) {
        return logRepository.findUnprocessedChunk(pageable);
    }

    // 동점처리용 최근 점수 조회
    public List<RecentGenreScore> findRecentScoresByGenres(Long userId, Collection<Genre> genres,
                                                           LocalDateTime since) {
        if (genres == null || genres.isEmpty()) return List.of();
        return logRepository.findRecentScoresByGenres(userId, genres, since);
    }


    /** 쓰기 작업 관련 메서드 */
    // (user, genre) raw_score 누적 UPSERT
    public void addRawScore(Long userId, Genre genre, long delta) {
        rawScoreRepository.upsertAdd(userId, genre, delta);
    }

    // raw_score 전체 삭제 (주기 초기화)
    public void deleteAllRawScores() {
        rawScoreRepository.deleteAllInBatch();
    }

    // 점수 로그 1건 저장
    public UserGenreScoreLog saveLog(UserGenreScoreLog log) {
        return logRepository.save(log);
    }

    // 미처리 로그를 처리 완료로 일괄 갱신
    public int markLogProcessedUntil(Long maxId, LocalDateTime now) {
        return logRepository.markProcessedUntil(maxId, now);
    }

    // 보존기간 지난 처리 로그 삭제
    public int deleteProcessedLogBefore(LocalDateTime threshold) {
        return logRepository.deleteProcessedBefore(threshold);
    }
}
