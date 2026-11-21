package com.storix.internal.aichat.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.favorite.service.FavoriteService;
import com.storix.domain.domains.genrescore.service.GenreScoreQueryService;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendationContext;
import com.storix.domain.domains.hashtag.service.HashtagCacheHelper;
import com.storix.domain.domains.hashtag.service.HashtagRecommendService;
import com.storix.domain.domains.library.service.LibraryService;
import com.storix.internal.aichat.controller.dto.AiUserContextResponse;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class InternalAiUserContextUseCase {

    private static final int HASHTAG_LIMIT = 10;

    private final GenreScoreQueryService genreScoreQueryService;
    private final HashtagRecommendService hashtagRecommendService;
    private final HashtagCacheHelper hashtagCacheHelper;
    private final LibraryService libraryService;
    private final FavoriteService favoriteService;

    // 해시태그는 홈 추천과 같은 개인화 계산. 활동이 없으면 비어 있다
    public AiUserContextResponse find(Long userId) {
        Long totalWorksCount = hashtagCacheHelper.findTotalWorksCount();
        if (totalWorksCount == null) {
            totalWorksCount = hashtagRecommendService.countAllWorks();
            hashtagCacheHelper.saveTotalWorksCount(totalWorksCount);
        }
        HashtagRecommendationContext context = hashtagRecommendService.collectRecommendationContext(userId, totalWorksCount);

        return AiUserContextResponse.of(
                genreScoreQueryService.getRawScores(userId),
                context.favoriteGenres(),
                hashtagRecommendService.calculatePersonalizedHashtags(context, HASHTAG_LIMIT),
                libraryService.findReviewedWorksIds(userId),
                favoriteService.findFavoriteWorksIds(userId));
    }
}
