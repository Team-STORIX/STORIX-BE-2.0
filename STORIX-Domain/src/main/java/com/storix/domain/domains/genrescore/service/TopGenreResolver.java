package com.storix.domain.domains.genrescore.service;

import com.storix.domain.domains.genrescore.adaptor.GenreScoreAdaptor;
import com.storix.domain.domains.genrescore.domain.UserGenreRawScore;
import com.storix.domain.domains.genrescore.dto.RecentGenreScore;
import com.storix.domain.domains.genrescore.dto.TopGenreInfo;
import com.storix.domain.domains.user.domain.Title;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// 유저의 대표 장르(가장 점수가 높은 장르)와 그 점수를 해석
// - 칭호가 정의된 장르만 대상 (칭호 없는 개그/액션/스포츠/감성은 제외)
// - 동점 장르가 둘 이상이면 최근 14일 획득 점수 -> 최신 획득 시각 순으로 대표 선택
@Service
@RequiredArgsConstructor
public class TopGenreResolver {

    private static final int TIE_BREAK_WINDOW_DAYS = 14;
    private static final Set<Genre> TITLED_GENRES = Title.titledGenres();

    private final GenreScoreAdaptor genreScoreAdaptor;

    @Transactional(readOnly = true)
    public Optional<TopGenreInfo> resolve(Long userId) {
        return resolve(userId, genreScoreAdaptor.findRawScoresByUserId(userId));
    }

    @Transactional(readOnly = true)
    public Map<Long, Optional<TopGenreInfo>> resolveAll(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();

        Map<Long, List<UserGenreRawScore>> scoresByUser = genreScoreAdaptor.findRawScoresByUserIds(userIds).stream()
                .filter(s -> TITLED_GENRES.contains(s.getGenre()))
                .collect(Collectors.groupingBy(UserGenreRawScore::getUserId));

        return userIds.stream()
                .distinct()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> resolve(userId, scoresByUser.getOrDefault(userId, List.of())),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private Optional<TopGenreInfo> resolve(Long userId, List<UserGenreRawScore> rawScores) {
        Map<Genre, Long> scoreByGenre = rawScores.stream()
                .filter(s -> TITLED_GENRES.contains(s.getGenre()))
                .collect(Collectors.toMap(UserGenreRawScore::getGenre, UserGenreRawScore::getRawScore));

        // 장르 점수 데이터가 아예 없을 때만 대표 장르 없음. 점수가 0이어도 최고점 장르는 반환
        if (scoreByGenre.isEmpty()) return Optional.empty();

        long max = Collections.max(scoreByGenre.values());

        List<Genre> topGenres = scoreByGenre.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey)
                .toList();

        Genre representative = topGenres.size() == 1 ? topGenres.get(0) : breakTie(userId, topGenres);
        return Optional.of(new TopGenreInfo(representative, max));
    }

    // 동점 장르 대표 선택
    private Genre breakTie(Long userId, List<Genre> candidates) {
        LocalDateTime since = LocalDateTime.now().minusDays(TIE_BREAK_WINDOW_DAYS);

        Map<Genre, RecentGenreScore> recentByGenre = genreScoreAdaptor.findRecentScoresByGenres(userId, candidates, since).stream()
                .collect(Collectors.toMap(RecentGenreScore::genre, Function.identity()));

        return candidates.stream()
                .sorted(Comparator
                        .comparingLong((Genre genre) -> recentScore(recentByGenre.get(genre))).reversed()
                        .thenComparing(
                                genre -> latestAt(recentByGenre.get(genre)),
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                        .thenComparingInt(Enum::ordinal))
                .findFirst()
                .orElseThrow();
    }

    private long recentScore(RecentGenreScore recent) {
        return recent == null || recent.score() == null ? 0L : recent.score();
    }

    private LocalDateTime latestAt(RecentGenreScore recent) {
        return recent == null ? null : recent.latestAt();
    }
}
