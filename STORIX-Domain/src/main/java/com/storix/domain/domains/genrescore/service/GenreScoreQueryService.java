package com.storix.domain.domains.genrescore.service;

import com.storix.domain.domains.genrescore.adaptor.GenreScoreAdaptor;
import com.storix.domain.domains.genrescore.domain.UserGenreRawScore;
import com.storix.domain.domains.preference.dto.GenreScoreInfo;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreScoreQueryService {

    // 아직 디자인 없는 장르: 개그, 액션, 스포츠, 감성 -> 관련 조회 시 노출 X
    private static final Set<Genre> HIDDEN_GENRES = EnumSet.of(
            Genre.GAG,
            Genre.ACTION,
            Genre.SPORTS,
            Genre.SENTIMENTAL
    );

    private final GenreScoreAdaptor genreScoreAdaptor;

    // 장르별 raw_score 반환
    public List<GenreScoreInfo> getRawScores(Long userId) {
        List<UserGenreRawScore> scores = genreScoreAdaptor.findRawScoresByUserId(userId);

        Map<Genre, Long> rawByGenre = scores.stream()
                .collect(Collectors.toMap(UserGenreRawScore::getGenre, UserGenreRawScore::getRawScore));

        return Arrays.stream(Genre.values())
                .filter(g -> !HIDDEN_GENRES.contains(g))
                .map(g -> new GenreScoreInfo(
                        g.getDbValue(),
                        (double) rawByGenre.getOrDefault(g, 0L)
                ))
                .sorted(Comparator.comparing(GenreScoreInfo::score).reversed())
                .toList();
    }
}
