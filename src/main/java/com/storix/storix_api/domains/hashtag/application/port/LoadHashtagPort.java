package com.storix.storix_api.domains.hashtag.application.port;

import com.storix.storix_api.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.storix_api.domains.works.domain.Genre;

import java.util.List;
import java.util.Set;

public interface LoadHashtagPort {
    List<HashtagRecommendResponseDto> recommendByGenres(Set<Genre> genres, int limit);
    List<HashtagRecommendResponseDto> recommendGlobalPopular(int limit);
}