package com.storix.domain.domains.hashtag.dto;

import com.storix.domain.domains.favorite.dto.FavoriteWorksWithCreatedAt;
import com.storix.domain.domains.preference.dto.ExplorationReactionWithCreatedAt;
import com.storix.domain.domains.works.domain.Genre;

import java.util.List;
import java.util.Set;

public record HashtagRecommendationContext(
        Long userId,
        Set<Genre> favoriteGenres,
        List<FavoriteWorksWithCreatedAt> favoriteWorks,
        List<ExplorationReactionWithCreatedAt> explorations,
        long totalWorksCount
) {
}
