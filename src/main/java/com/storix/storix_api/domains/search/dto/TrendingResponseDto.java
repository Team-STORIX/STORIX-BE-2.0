package com.storix.storix_api.domains.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TrendingResponseDto {

    private List<TrendingItem> trendingKeywords;
}
