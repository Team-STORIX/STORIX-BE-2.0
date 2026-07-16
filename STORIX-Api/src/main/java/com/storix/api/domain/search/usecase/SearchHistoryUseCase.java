package com.storix.api.domain.search.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.search.dto.RecentResponseDto;
import com.storix.domain.domains.search.dto.TrendingResponseDto;
import com.storix.domain.domains.search.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class SearchHistoryUseCase {

    private final SearchHistoryService searchHistoryService;

    // 급상승 검색어 조회
    public CustomResponse<TrendingResponseDto> getTrending() {
        TrendingResponseDto result = TrendingResponseDto.builder()
                .trendingKeywords(searchHistoryService.getTrendingKeywords())
                .build();

        return CustomResponse.onSuccess(SuccessCode.SUCCESS, result);
    }

    // 최근 검색어 조회
    public CustomResponse<RecentResponseDto> getRecent(Long userId) {
        RecentResponseDto result = RecentResponseDto.builder()
                .recentKeywords(searchHistoryService.getRecentKeywords(userId))
                .build();

        return CustomResponse.onSuccess(SuccessCode.SUCCESS, result);
    }

    // 최근 검색어 삭제
    public CustomResponse<Void> deleteRecent(Long userId, String keyword) {
        searchHistoryService.deleteRecentKeyword(userId, keyword);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS, null);
    }

    // 최근 검색어 전체 삭제
    public CustomResponse<Void> deleteAllRecent(Long userId) {
        searchHistoryService.deleteAllRecentKeywords(userId);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS, null);
    }
}
