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

    private final UserGenreRawScoreRepository rawScoreRepository;

    // 장르별 raw_score 반환
    public List<GenreScoreInfo> getRawScores(Long userId) {
        List<UserGenreRawScore> scores = rawScoreRepository.findAllByIdUserId(userId);

        Map<Genre, Long> rawByGenre = scores.stream()
                .collect(Collectors.toMap(UserGenreRawScore::getGenre, UserGenreRawScore::getRawScore));

        return Arrays.stream(Genre.values())
                .map(g -> new GenreScoreInfo(
                        g.getDbValue(),
                        (double) rawByGenre.getOrDefault(g, 0L)
                ))
                .sorted(Comparator.comparing(GenreScoreInfo::score).reversed())
                .toList();
    }
}
