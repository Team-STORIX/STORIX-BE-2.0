package com.storix.api.domain.search.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.WorksSearchResponseDto;
import com.storix.domain.domains.search.service.SearchHistoryService;
import com.storix.domain.domains.search.service.SearchService;
import com.storix.domain.domains.topicroom.application.usecase.TopicRoomUseCase;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.WorksType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class SearchUseCase {

    private final SearchService searchService;
    private final SearchHistoryService searchHistoryService;
    private final TopicRoomUseCase topicRoomUseCase;

    // 작품 탭 검색
    public CustomResponse<SearchResponseWrapperDto<WorksSearchResponseDto>> searchWorks(Long userId, String keyword, Pageable pageable) {
        Slice<WorksSearchResponseDto> result = searchService.searchWorks(userId, keyword, pageable);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS, wrapWithFallback(result));
    }

    // 작품 탭 필터 검색
    public CustomResponse<SearchResponseWrapperDto<WorksSearchResponseDto>> searchWorksWithFilters(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable) {
        Slice<WorksSearchResponseDto> result =
                searchService.searchWorksWithFilters(userId, keyword, worksTypes, genres, pageable);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS, wrapWithFallback(result));
    }

    // [+] 탭 검색
    public CustomResponse<PlusSearchResponseWrapperDto<WorksSearchResponseDto>> searchWorksForWriting(String keyword, Pageable pageable) {
        if (pageable.getPageNumber() == 0) {
            searchHistoryService.addTrendingScore(keyword);
        }

        PlusSearchResponseWrapperDto<WorksSearchResponseDto> result = searchService.searchWorksForWriting(keyword, pageable);

        return CustomResponse.onSuccess(SuccessCode.PLUS_WORKS_LOAD_SUCCESS, result);
    }

    // 토픽룸 탭 검색
    public CustomResponse<PlusSearchResponseWrapperDto<TopicRoomResponseDto>> searchTopicRooms(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable) {
        if (pageable.getPageNumber() == 0) {
            searchHistoryService.addTrendingScore(keyword);
        }

        PlusSearchResponseWrapperDto<TopicRoomResponseDto> result =
                topicRoomUseCase.searchRoomsWithFilters(userId, keyword, worksTypes, genres, pageable);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS, result);
    }


    // 검색 결과가 없으면 추천 검색어를 함께 담아 래핑
    private SearchResponseWrapperDto<WorksSearchResponseDto> wrapWithFallback(Slice<WorksSearchResponseDto> result) {
        String fallbackKeyword = result.isEmpty() ? searchHistoryService.getFallbackRecommendation() : null;

        return SearchResponseWrapperDto.<WorksSearchResponseDto>builder()
                .result(result)
                .fallbackRecommendation(fallbackKeyword)
                .build();
    }
}
