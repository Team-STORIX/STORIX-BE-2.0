package com.storix.api.domain.search.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.WorksSearchResponseDto;
import com.storix.domain.domains.search.service.SearchHistoryService;
import com.storix.domain.domains.search.service.SearchService;
import com.storix.domain.domains.topicroom.service.TopicRoomService;
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
    private final TopicRoomService topicRoomService;

    // 작품 탭 검색
    public CustomResponse<SearchResponseWrapperDto<WorksSearchResponseDto>> searchWorks(Long userId, String keyword, Pageable pageable) {
        if (keyword != null && pageable.getPageNumber() == 0) {
            searchHistoryService.addSearchLog(userId, keyword);
            searchHistoryService.addTrendingScore(keyword, null, null);
        }

        Slice<WorksSearchResponseDto> result = searchService.searchWorks(userId, keyword, pageable);

        String fallback = result.isEmpty() ? searchHistoryService.getFallbackRecommendation() : null;
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, SearchResponseWrapperDto.of(result, fallback));
    }

    // 작품 탭 필터 검색
    public CustomResponse<SearchResponseWrapperDto<WorksSearchResponseDto>> searchWorksWithFilters(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable) {
        if (keyword != null && pageable.getPageNumber() == 0) {
            searchHistoryService.addSearchLog(userId, keyword);
            searchHistoryService.addTrendingScore(keyword, worksTypes, genres);
        }

        Slice<WorksSearchResponseDto> result =
                searchService.searchWorksWithFilters(userId, keyword, worksTypes, genres, pageable);

        String fallback = result.isEmpty() ? searchHistoryService.getFallbackRecommendation() : null;
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, SearchResponseWrapperDto.of(result, fallback));
    }

    // [+] 탭 검색
    public CustomResponse<PlusSearchResponseWrapperDto<WorksSearchResponseDto>> searchWorksForWriting(Long userId, String keyword, Pageable pageable) {
        if (pageable.getPageNumber() == 0) {
            searchHistoryService.addTrendingScore(keyword, null, null);
        }

        PlusSearchResponseWrapperDto<WorksSearchResponseDto> result = searchService.searchWorksForWriting(userId, keyword, pageable);

        return CustomResponse.onSuccess(SuccessCode.PLUS_WORKS_LOAD_SUCCESS, result);
    }

    // 토픽룸 탭 검색
    public CustomResponse<PlusSearchResponseWrapperDto<TopicRoomResponseDto>> searchTopicRooms(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable) {
        if (pageable.getPageNumber() == 0) {
            searchHistoryService.addTrendingScore(keyword, worksTypes, genres);
        }

        PlusSearchResponseWrapperDto<TopicRoomResponseDto> result =
                topicRoomService.searchRoomsWithFilters(userId, keyword, worksTypes, genres, pageable);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS, result);
    }

}
