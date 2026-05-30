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
    private final HashtagCacheHelper hashtagCacheHelper;

    public HashtagRecommendationContext collectRecommendationContext(Long userId) {

        User user = userAdaptor.findUserById(userId);

        return new HashtagRecommendationContext(
                userId,
                user.getFavoriteGenreList(),
                favoriteWorksAdaptor.findAllWithCreatedAtByUserId(userId),
                explorationAdaptor.findExplorationsWithCreatedAtByUserId(userId),
                hashtagCacheHelper.getOrLoadTotalWorksCount(workAdaptor::countAllWorks)
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

        // 행동 이력이 없으면 개인화 추천 불가
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
        // 작품에 해시태그가 하나도 없으면 점수 계산 대상 없음
        if (hashtagsByWorksId.isEmpty()) {
            return List.of();
        }

        // 4. 행동 점수와 time decay를 반영해 해시태그별 raw score를 누적
        Map<Long, HashtagScore> scoreMap = calculateRawScores(actions, hashtagsByWorksId);

        // 5. 후보 해시태그별 전체 작품 등장 빈도를 조회해 IDF 보정에 사용
        Map<Long, Long> documentFrequencyMap = hashtagCacheHelper.getOrLoadDocumentFrequencies(
                scoreMap.keySet(),
                hashtagAdaptor::findDocumentFrequencyMap
        );

        // 6. raw score에 IDF를 곱해 범용 태그 쏠림을 완화한 뒤 랭킹
        return rankPersonalizedHashtags(
                scoreMap,
                documentFrequencyMap,
                context.totalWorksCount(),
                recommendationLimit
        );
    }

    // 관심 작품(+5)과 탐색 반응(좋아요 +4 / 싫어요 -3)을 동일한 행동 단위로 변환
    // 두 신호를 하나의 리스트로 합쳐 이후 점수 계산을 단일 루프로 처리할 수 있도록 함
    private List<HashtagRecommendationAction> toRecommendationActions(
            List<FavoriteWorksWithCreatedAt> favoriteWorks,
            List<ExplorationReactionWithCreatedAt> explorations
    ) {
        List<HashtagRecommendationAction> actions = new ArrayList<>();

        // 관심 작품은 가장 강한 긍정 신호 → 고정 점수 +5
        favoriteWorks.forEach(info -> actions.add(new HashtagRecommendationAction(
                info.worksId(),
                FAVORITE_WORK_SCORE,
                info.createdAt()
        )));

        // 탐색 반응은 좋아요/싫어요에 따라 긍정(+4) 또는 부정(-3) 점수 부여
        explorations.forEach(info -> actions.add(new HashtagRecommendationAction(
                info.worksId(),
                info.isLiked() ? EXPLORATION_LIKE_SCORE : EXPLORATION_DISLIKE_SCORE,
                info.createdAt()
        )));

        return actions;
    }

    // 각 행동에 time decay를 적용한 점수를, 해당 작품에 달린 해시태그들에 누적
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
                // 해시태그가 처음 등장하면 scoreMap에 새 항목 생성
                HashtagScore score = scoreMap.computeIfAbsent(
                        hashtag.hashtagId(),
                        id -> new HashtagScore(hashtag.hashtagId(), hashtag.hashtagName())
                );

                // 동일 해시태그가 여러 작품에 달려 있으면 점수 누적
                score.addRawScore(actionScore);

                // 싫어요(-3) 행동은 positiveCount에 포함하지 않음
                // positiveCount는 이후 랭킹 동점 처리 시 보조 정렬 기준으로 사용
                if (action.baseScore() > 0) {
                    score.increasePositiveCount();
                }
            }
        }
        return scoreMap;
    }

    // rawScore에 IDF를 곱해 최종 점수를 계산하고, 장르명 태그를 제외한 뒤 상위 N개를 추출
    // 정렬 우선순위: 최종 점수 내림차순 → 긍정 행동 횟수 내림차순 → 이름 오름차순(동점 안정 정렬)
    private List<HashtagRecommendResponseDto> rankPersonalizedHashtags(
            Map<Long, HashtagScore> scoreMap,
            Map<Long, Long> documentFrequencyMap,
            long totalWorksCount,
            int recommendationLimit
    ) {
        Set<String> genreNames = getGenreNames();

        return scoreMap.values().stream()
                // 싫어요가 더 많아 음수가 된 태그는 추천 대상에서 제외
                .filter(score -> score.rawScore > 0)
                // 이름이 없는 태그는 노출 불가
                .filter(score -> score.name != null && !score.name.isBlank())
                // 장르명과 동일한 태그는 너무 포괄적이어서 제외 (예: "로맨스", "스릴러")
                .filter(score -> !genreNames.contains(score.name.trim()))
                .map(score -> new HashtagRank(
                        score.id,
                        score.name,
                        // IDF 보정: 흔한 태그의 점수를 낮춰 다양한 추천 유도
                        calculateFinalScore(
                                score.rawScore,
                                totalWorksCount,
                                documentFrequencyMap.getOrDefault(score.id, 0L)
                        ),
                        score.positiveCount
                ))
                // IDF 보정 후에도 0 이하가 된 태그는 제외
                .filter(rank -> rank.finalScore() > 0)
                .sorted(Comparator
                        .comparing(HashtagRank::finalScore, Comparator.reverseOrder())
                        .thenComparing(HashtagRank::positiveCount, Comparator.reverseOrder())
                        .thenComparing(HashtagRank::name))
                .limit(recommendationLimit)
                .map(HashtagRank::toResponse)
                .toList();
    }

    // IDF: 전체 작품 중 해당 태그가 등장하는 작품 수가 많을수록 점수를 낮춤
    private double calculateFinalScore(double rawScore, long totalWorksCount, long documentFrequency) {
        if (totalWorksCount <= 0) {
            return rawScore;
        }

        double idf = Math.log((totalWorksCount + 1.0) / (documentFrequency + 1.0)) + 1.0;
        return rawScore * idf;
    }

    // 반감기(30일) 기반 지수 감소: 행동이 오래될수록 영향력이 절반씩 줄어듦
    private double calculateTimeWeight(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 1.0;
        }

        long ageDays = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        // 미래 시각이 들어오는 이상 케이스 방어
        if (ageDays < 0) {
            ageDays = 0;
        }

        return Math.pow(0.5, ageDays / HALF_LIFE_DAYS);
    }

    public List<HashtagRecommendResponseDto> fillWithFallback(
            List<HashtagRecommendResponseDto> personalized,
            Set<Genre> favoriteGenres,
            int recommendationLimit
    ) {
        // LinkedHashMap으로 삽입 순서를 유지하면서 id 기준 중복 제거
        Map<Long, HashtagRecommendResponseDto> result = new LinkedHashMap<>();
        Set<String> genreNames = getGenreNames();

        // 개인화 결과 채우기
        appendRecommendations(result, personalized, genreNames, recommendationLimit);

        // 개인화 결과가 부족하면 온보딩 장르 기반 인기 태그 보충
        if (result.size() < recommendationLimit && favoriteGenres != null && !favoriteGenres.isEmpty()) {
            appendRecommendations(
                    result,
                    hashtagAdaptor.recommendByGenres(favoriteGenres, recommendationLimit - result.size()),
                    genreNames,
                    recommendationLimit
            );
        }

        // 그래도 부족하면 전체 인기 태그로 최종 보충
        if (result.size() < recommendationLimit) {
            appendRecommendations(
                    result,
                    hashtagAdaptor.recommendGlobalPopular(recommendationLimit - result.size()),
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
            // 목표 개수에 도달하면 즉시 중단
            if (result.size() >= recommendationLimit) {
                return;
            }
            if (candidate.name() == null || candidate.name().isBlank()) {
                continue;
            }
            // 장르명 태그는 fallback 단계에서도 제외
            if (genreNames.contains(candidate.name().trim())) {
                continue;
            }

            // 이미 result에 있는 태그 중복 방지
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