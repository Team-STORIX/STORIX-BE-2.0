package com.storix.domain.domains.genrescore.service;

import com.storix.domain.domains.genrescore.domain.UserGenreRawScore;
import com.storix.domain.domains.genrescore.repository.UserGenreRawScoreRepository;
import com.storix.domain.domains.preference.dto.GenreScoreInfo;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreScoreQueryService {

    private static final double PERCENT_SCALE = 100.0;

    private final UserGenreRawScoreRepository rawScoreRepository;

    // 장르별 raw_score 비율 정규화 (전체 합 대비 백분율 0~100, 정수 반올림 변환)
    public List<GenreScoreInfo> getRatioNormalized(Long userId) {
        List<UserGenreRawScore> scores = rawScoreRepository.findAllByIdUserId(userId);
        return normalize(scores);
    }

    private List<GenreScoreInfo> normalize(List<UserGenreRawScore> scores) {
        long total = scores.stream().mapToLong(UserGenreRawScore::getRawScore).sum();

        Map<Genre, Long> rawByGenre = scores.stream()
                .collect(Collectors.toMap(UserGenreRawScore::getGenre, UserGenreRawScore::getRawScore));

        return Arrays.stream(Genre.values())
                .map(g -> {
                    long raw = rawByGenre.getOrDefault(g, 0L);
                    double score = total == 0 ? 0.0 : Math.round(raw / (double) total * PERCENT_SCALE);
                    return new GenreScoreInfo(g.getDbValue(), score);
                })
                .sorted(Comparator.comparing(GenreScoreInfo::score).reversed())
                .toList();
    }
}
