package com.storix.domain.domains.hashtag.application.port;

import com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.domain.domains.works.domain.Genre;

import java.util.List;
import java.util.Set;

public interface LoadHashtagPort {
    List<HashtagRecommendResponseDto> recommendByGenres(Set<Genre> genres, int limit);
    List<HashtagRecommendResponseDto> recommendGlobalPopular(int limit);
}