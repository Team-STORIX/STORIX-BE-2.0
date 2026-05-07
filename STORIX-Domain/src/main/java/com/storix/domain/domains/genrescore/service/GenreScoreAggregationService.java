package com.storix.domain.domains.genrescore.service;

import com.storix.domain.domains.genrescore.dto.UnprocessedLogRow;
import com.storix.domain.domains.genrescore.repository.UserGenreRawScoreRepository;
import com.storix.domain.domains.genrescore.repository.UserGenreScoreLogRepository;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GenreScoreAggregationService {

    public static final int DEFAULT_CHUNK_SIZE = 1000;
    public static final int LOG_RETENTION_DAYS = 30;

    private final UserGenreScoreLogRepository logRepository;
    private final UserGenreRawScoreRepository rawScoreRepository;

    // 미처리 로그를 청크 단위로 처리
    @Transactional
    public ChunkResult processChunk(int chunkSize) {
        // 1. 미처리 로그 조회
        List<UnprocessedLogRow> rows = logRepository.findUnprocessedChunk(PageRequest.of(0, chunkSize));
        if (rows.isEmpty()) return ChunkResult.empty();

        // 2. (user, genre)별 가중치 합산
        Map<UserGenreKey, Long> sums = new HashMap<>();
        Set<Long> users = new HashSet<>();
        long maxId = 0L;

        for (UnprocessedLogRow row : rows) {
            sums.merge(new UserGenreKey(row.userId(), row.genre()), row.weight().longValue(), Long::sum);
            users.add(row.userId());
            if (row.id() > maxId) maxId = row.id();
        }

        // 3. raw_score 테이블 UPSERT
        sums.forEach((key, sum) -> rawScoreRepository.upsertAdd(key.userId(), key.genre(), sum));

        // 4. 로그 처리
        logRepository.markProcessedUntil(maxId, LocalDateTime.now());

        return new ChunkResult(rows.size(), sums.size(), users);
    }

    // 처리된 로그 삭제
    @Transactional
    public int cleanupOldProcessed() {
        return logRepository.deleteProcessedBefore(LocalDateTime.now().minusDays(LOG_RETENTION_DAYS));
    }

    public record ChunkResult(int processedLogs, int groups, Set<Long> users) {
        public static ChunkResult empty() {
            return new ChunkResult(0, 0, Collections.emptySet());
        }
    }

    private record UserGenreKey(Long userId, Genre genre) {}
}
