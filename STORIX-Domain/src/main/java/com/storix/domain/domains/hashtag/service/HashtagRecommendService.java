package com.storix.domain.domains.hashtag.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.favorite.dto.FavoriteWorksWithCreatedAt;
import com.storix.domain.domains.hashtag.dto.HashtagInfo;
import com.storix.domain.domains.hashtag.adaptor.HashtagAdaptor;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendationContext;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.domain.domains.preference.adaptor.ExplorationAdaptor;
import com.storix.domain.domains.preference.dto.ExplorationReactionWithCreatedAt;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HashtagRecommendService {

    private static final double FAVORITE_WORK_SCORE = 5.0;
    private static final double EXPLORATION_LIKE_SCORE = 4.0;
    private static final double EXPLORATION_DISLIKE_SCORE = -3.0;
    private static final double HALF_LIFE_DAYS = 30.0;

    private final HashtagAdaptor hashtagAdaptor;
    private final UserAdaptor userAdaptor;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;
    private final ExplorationAdaptor explorationAdaptor;
    private final WorksAdaptor workAdaptor;

    public List<HashtagRecommendResponseDto> getGlobalPopularHashtags(int recommendationLimit) {
        return hashtagAdaptor.recommendGlobalPopular(recommendationLimit);
    }

    public HashtagRecommendationContext collectRecommendationContext(Long userId) {

        User user = userAdaptor.findUserById(userId);

        return new HashtagRecommendationContext(
                userId,
                user.getFavoriteGenreList(),
                favoriteWorksAdaptor.findAllWithCreatedAtByUserId(userId),
                explorationAdaptor.findExplorationsWithCreatedAtByUserId(userId),
                workAdaptor.countAllWorks()
        );
    }

    public List<HashtagRecommendResponseDto> calculatePersonalizedHashtags(
            HashtagRecommendationContext context,
            int recommendationLimit
    ) {

        // 1. 관심 작품과 취향 탐색 반응을 추천 점수 계산용 행동 단위로 변환
        List<HashtagRecommendationAction> actions = toRecommendationActions(
                context.favoriteWorks(),
                context.explorations()
        );

        if (actions.isEmpty()) {
            return List.of();
        }

        // 2. 행동이 발생한 작품 ID를 중복 없이 수집
        List<Long> worksIds = actions.stream()
                .map(HashtagRecommendationAction::worksId)
                .distinct()
                .toList();

        // 3. 작품별 해시태그 조회
        Map<Long, List<HashtagInfo>> hashtagsByWorksId = hashtagAdaptor.findHashtagInfosByWorksIds(worksIds);
        if (hashtagsByWorksId.isEmpty()) {
            return List.of();
        }

        // 4. 행동 점수와 time decay를 반영해 해시태그별 raw score를 누적
        Map<Long, HashtagScore> scoreMap = calculateRawScores(actions, hashtagsByWorksId);

        // 5. 후보 해시태그별 전체 작품 등장 빈도를 조회해 IDF 보정에 사용
        Map<Long, Long> documentFrequencyMap = hashtagAdaptor.findDocumentFrequencyMap(scoreMap.keySet());

        // 6. raw score에 IDF를 곱해 범용 태그 쏠림을 완화한 뒤 랭킹
        return rankPersonalizedHashtags(
                scoreMap,
                documentFrequencyMap,
                context.totalWorksCount(),
                recommendationLimit
        );
    }

    private List<HashtagRecommendationAction> toRecommendationActions(
            List<FavoriteWorksWithCreatedAt> favoriteWorks,
            List<ExplorationReactionWithCreatedAt> explorations
    ) {
        List<HashtagRecommendationAction> actions = new ArrayList<>();

        favoriteWorks.forEach(info -> actions.add(new HashtagRecommendationAction(
                info.worksId(),
                FAVORITE_WORK_SCORE,
                info.createdAt()
        )));

        explorations.forEach(info -> actions.add(new HashtagRecommendationAction(
                info.worksId(),
                info.isLiked() ? EXPLORATION_LIKE_SCORE : EXPLORATION_DISLIKE_SCORE,
                info.createdAt()
        )));

        return actions;
    }

    private Map<Long, HashtagScore> calculateRawScores(
            List<HashtagRecommendationAction> actions,
            Map<Long, List<HashtagInfo>> hashtagsByWorksId
    ) {
        Map<Long, HashtagScore> scoreMap = new HashMap<>();

        for (HashtagRecommendationAction action : actions) {
            List<HashtagInfo> hashtags = hashtagsByWorksId.getOrDefault(action.worksId(), List.of());

            // 행동별 기본 점수에 time decay를 적용
            double actionScore = action.baseScore() * calculateTimeWeight(action.createdAt());

            for (HashtagInfo hashtag : hashtags) {
                HashtagScore score = scoreMap.computeIfAbsent(
                        hashtag.hashtagId(),
                        id -> new HashtagScore(hashtag.hashtagId(), hashtag.hashtagName())
                );

                score.addRawScore(actionScore);
                if (action.baseScore() > 0) {
                    score.increasePositiveCount();
                }
            }
        }
        return scoreMap;
    }

    private List<HashtagRecommendResponseDto> rankPersonalizedHashtags(
            Map<Long, HashtagScore> scoreMap,
            Map<Long, Long> documentFrequencyMap,
            long totalWorksCount,
            int recommendationLimit
    ) {
        Set<String> genreNames = getGenreNames();

        return scoreMap.values().stream()
                .filter(score -> score.rawScore > 0)
                .filter(score -> score.name != null && !score.name.isBlank())
                .filter(score -> !genreNames.contains(score.name.trim()))
                .map(score -> new HashtagRank(
                        score.id,
                        score.name,
                        calculateFinalScore(
                                score.rawScore,
                                totalWorksCount,
                                documentFrequencyMap.getOrDefault(score.id, 0L)
                        ),
                        score.positiveCount
                ))
                .filter(rank -> rank.finalScore() > 0)
                .sorted(Comparator
                        .comparing(HashtagRank::finalScore, Comparator.reverseOrder())
                        .thenComparing(HashtagRank::positiveCount, Comparator.reverseOrder())
                        .thenComparing(HashtagRank::name))
                .limit(recommendationLimit)
                .map(HashtagRank::toResponse)
                .toList();
    }

    private double calculateFinalScore(double rawScore, long totalWorksCount, long documentFrequency) {
        if (totalWorksCount <= 0) {
            return rawScore;
        }

        double idf = Math.log((totalWorksCount + 1.0) / (documentFrequency + 1.0)) + 1.0;
        return rawScore * idf;
    }

    private double calculateTimeWeight(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 1.0;
        }

        long ageDays = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        if (ageDays < 0) {
            ageDays = 0;
        }

        // 반감기마다 행동 영향력이 절반으로 줄어듬
        return Math.pow(0.5, ageDays / HALF_LIFE_DAYS);
    }

    public List<HashtagRecommendResponseDto> fillWithFallback(
            List<HashtagRecommendResponseDto> personalized,
            Set<Genre> favoriteGenres,
            int recommendationLimit
    ) {
        Map<Long, HashtagRecommendResponseDto> result = new LinkedHashMap<>();
        Set<String> genreNames = getGenreNames();

        appendRecommendations(result, personalized, genreNames, recommendationLimit);

        // 개인화 결과가 부족하면 온보딩 장르 기반 인기 태그를 먼저 보충
        if (result.size() < recommendationLimit && favoriteGenres != null && !favoriteGenres.isEmpty()) {
            appendRecommendations(
                    result,
                    hashtagAdaptor.recommendByGenres(favoriteGenres, recommendationLimit),
                    genreNames,
                    recommendationLimit
            );
        }

        // 그래도 부족하면 전체 인기 태그로 최종 보충
        if (result.size() < recommendationLimit) {
            appendRecommendations(
                    result,
                    hashtagAdaptor.recommendGlobalPopular(recommendationLimit),
                    genreNames,
                    recommendationLimit
            );
        }

        return result.values().stream()
                .limit(recommendationLimit)
                .toList();
    }

    private void appendRecommendations(
            Map<Long, HashtagRecommendResponseDto> result,
            List<HashtagRecommendResponseDto> candidates,
            Set<String> genreNames,
            int recommendationLimit
    ) {
        for (HashtagRecommendResponseDto candidate : candidates) {
            if (result.size() >= recommendationLimit) {
                return;
            }
            if (candidate.name() == null || candidate.name().isBlank()) {
                continue;
            }
            if (genreNames.contains(candidate.name().trim())) {
                continue;
            }

            result.putIfAbsent(candidate.id(), candidate);
        }
    }

    private Set<String> getGenreNames() {
        return Arrays.stream(Genre.values())
                .map(Genre::getDbValue)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private record HashtagRank(
            Long id,
            String name,
            double finalScore,
            int positiveCount
    ) {
        private HashtagRecommendResponseDto toResponse() {
            return new HashtagRecommendResponseDto(id, name, Math.round(finalScore));
        }
    }

    private record HashtagRecommendationAction(
            Long worksId,
            double baseScore,
            LocalDateTime createdAt
    ) {
    }

    private static class HashtagScore {
        private final Long id;
        private final String name;
        private double rawScore;
        private int positiveCount;

        private HashtagScore(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        private void addRawScore(double score) {
            this.rawScore += score;
        }

        private void increasePositiveCount() {
            this.positiveCount++;
        }

    }
}
