package com.storix.domain.domains.profile.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.hashtag.adaptor.HashtagAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.plus.domain.Rating;
import com.storix.domain.domains.plus.dto.RatingCountInfo;
import com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo;
import com.storix.domain.domains.profile.dto.FavoriteHashtagsResponse;
import com.storix.domain.domains.profile.dto.FavoriteWorksWithReviewInfo;
import com.storix.domain.domains.profile.dto.RatingCountResponse;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.dto.WorksInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileFavoriteService {

    private final FavoriteWorksAdaptor favoriteWorksAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final HashtagAdaptor hashtagAdaptor;

    private final LoadWorksPort loadWorksPort;

    // 관심 작품 등록수 조회
    @Transactional(readOnly = true)
    public int findTotalFavoriteWorksCount(Long userId) {
        return favoriteWorksAdaptor.countFavoriteWorks(userId);
    }

    // 관심 작품 정보 조회
    @Transactional(readOnly = true)
    public Slice<FavoriteWorksWithReviewInfo> findAllFavoriteWorksInfo(Long userId, Pageable pageable) {

        // 관심 작품 등록 리스트 조회
        Slice<Long> worksIdsSlice = favoriteWorksAdaptor.findSliceFavoriteWorksId(userId, pageable);
        List<Long> worksIds = worksIdsSlice.getContent();

        if (worksIds.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, worksIdsSlice.hasNext());
        }

        // 1) 관심 작품 정보 조회
        Map<Long, WorksInfo> worksMap =
                loadWorksPort.findAllWorksInfoByWorksIds(worksIds);

        // 2) 리뷰 관련 정보 조회
        List<ReviewedWorksIdAndRatingInfo> reviewedList =
                reviewAdaptor.findAllReviewInfoByFavoriteWorks(userId, worksIds);

        Map<Long, Rating> ratingMap = reviewedList.stream()
                .collect(Collectors.toMap(
                        ReviewedWorksIdAndRatingInfo::worksId,
                        ReviewedWorksIdAndRatingInfo::rating,
                        (existing, replacement) -> existing
                ));

        // 관심 작품 등록 순으로 1) 관심 작품 정보, 2) 리뷰 관련 정보 정렬
        List<FavoriteWorksWithReviewInfo> ordered = worksIds.stream()
                .map(worksId -> {
                    WorksInfo worksInfo = worksMap.get(worksId);
                    if (worksInfo == null) return null;

                    Rating ratingEnum = ratingMap.get(worksId);
                    boolean isReviewed = ratingEnum != null;
                    String rating = isReviewed ? ratingEnum.getDbValue() : null;

                    return FavoriteWorksWithReviewInfo.of(worksInfo, isReviewed, rating);
                })
                .filter(Objects::nonNull)
                .toList();

        // 관심 작품 정보 리스트
        return new SliceImpl<>(ordered, pageable, worksIdsSlice.hasNext());
    }

    // 별점 분포 조회
    @Transactional(readOnly = true)
    public RatingCountResponse findRatingDistributionByUserId(Long userId) {

        List<RatingCountInfo> raws = reviewAdaptor.countByRating(userId);

        Map<String, Long> result = Arrays.stream(Rating.values())
                .collect(Collectors.toMap(
                        Rating::getDbValue,
                        r -> 0L,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        for (RatingCountInfo dto : raws) {
            result.put(dto.rating().getDbValue(), dto.count());
        }

        return RatingCountResponse.of(result);
    }

    // 선호 해시태그 조회
    @Transactional(readOnly = true)
    public FavoriteHashtagsResponse findFavoriteHashtagsByUserId(Long userId) {

        // 결과 Map
        Map<Integer, String> rankingMap = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            rankingMap.put(i, null);
        }

        // 1) 관심 작품 id
        List<Long> worksIds = favoriteWorksAdaptor.findAllFavoriteWorksIdsByUserId(userId);
        if (worksIds == null || worksIds.isEmpty()) {
            return new FavoriteHashtagsResponse(Map.of());
        }

        // 고평점 작품 id
        List<Long> worksIdsByHighRatings = reviewAdaptor.findWorksIdsByHighRatings(userId);

        // 2) 해시태그 정보
        Map<Long, List<String>> hashTags = hashtagAdaptor.findHashTagsByWorksIds(worksIds);
        if (hashTags == null || hashTags.isEmpty()) {
            return new FavoriteHashtagsResponse(Map.of());
        }

        // 관심 작품으로 등록된 고평점 작품의 해시태그 정보
        Set<Long> highRatedWorksSet = new HashSet<>(worksIdsByHighRatings == null ? List.of() : worksIdsByHighRatings);
        Set<String> highRatedTagSet = hashTags.entrySet().stream()
                .filter(e -> highRatedWorksSet.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        // 장르 문자열 정보
        Set<String> genreValues = Arrays.stream(Genre.values())
                .map(Genre::getDbValue)
                .collect(Collectors.toSet());

        // 3) 빈도 집계
        Map<String, Long> freq = hashTags.values().stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(String::trim)
                .filter(s -> !s.isEmpty()) // 빈도 2 이상
                .filter(s -> !genreValues.contains(s)) // 장르 해시태그 제외
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 4) 정렬 및 랭킹 매핑
        List<String> rankedTags = freq.entrySet().stream()
                .filter(e -> e.getValue() >= 2)
                .sorted(
                        Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed()
                                .thenComparing(e -> highRatedTagSet.contains(e.getKey()) ? 0 : 1)
                                .thenComparing(Map.Entry::getKey)
                )
                .map(Map.Entry::getKey)
                .limit(5)
                .toList();

        // 최종 매핑
        for (int i = 0; i < rankedTags.size(); i++) {
            rankingMap.put(i + 1, rankedTags.get(i));
        }
        return new FavoriteHashtagsResponse(rankingMap);
    }
}
