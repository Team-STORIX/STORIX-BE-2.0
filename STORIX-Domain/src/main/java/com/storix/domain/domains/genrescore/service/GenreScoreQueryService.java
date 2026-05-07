package com.storix.domain.domains.genrescore.service;

import com.storix.domain.domains.genrescore.domain.UserGenreRawScore;
import com.storix.domain.domains.genrescore.repository.UserGenreRawScoreRepository;
import com.storix.domain.domains.preference.dto.GenreScoreInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreScoreQueryService {

    private static final double PERCENT_SCALE = 100.0;

    private final UserGenreRawScoreRepository rawScoreRepository;

    // 장르별 raw_score 비율 정규화 (전체 합 대비 백분율 0~100, 정수 반올림 변환)
    @Transactional(readOnly = true)
    public List<GenreScoreInfo> getRatioNormalized(Long userId) {
        return normalize(rawScoreRepository.findAllByIdUserId(userId));
    }

    private List<GenreScoreInfo> normalize(List<UserGenreRawScore> scores) {
        long total = scores.stream().mapToLong(UserGenreRawScore::getRawScore).sum();
        if (total == 0) return Collections.emptyList();

        return scores.stream()
                .map(s -> new GenreScoreInfo(
                        s.getGenre().getDbValue(),
                        (double) Math.round(s.getRawScore() / (double) total * PERCENT_SCALE)
                ))
                .toList();
    }
}
