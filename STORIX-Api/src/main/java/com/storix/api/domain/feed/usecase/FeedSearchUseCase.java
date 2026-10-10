package com.storix.api.domain.feed.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.feed.service.FeedService;
import com.storix.domain.domains.profile.dto.ReaderBoardWithProfileInfo;
import com.storix.domain.domains.search.dto.RecentResponseDto;
import com.storix.domain.domains.search.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@UseCase
@RequiredArgsConstructor
public class FeedSearchUseCase {

    private final FeedService feedService;
    private final SearchHistoryService searchHistoryService;

    // 피드 게시글 검색
    public CustomResponse<Slice<ReaderBoardWithProfileInfo>> searchReaderBoards(Long userId, String keyword, Pageable pageable) {

        // 0. 첫 페이지 검색어만 최근 검색어로 남김
        if (pageable.getPageNumber() == 0) {
            searchHistoryService.addFeedSearchLog(userId, keyword);
        }

        // 1. 관련도순 검색
        Slice<ReaderBoardWithProfileInfo> result = feedService.searchReaderBoards(userId, keyword, pageable);
        return CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_SEARCH_SUCCESS, result);
    }

    // 피드 최근 검색어 조회
    public CustomResponse<RecentResponseDto> getRecentKeywords(Long userId) {

        RecentResponseDto result = RecentResponseDto.builder()
                .recentKeywords(searchHistoryService.getFeedRecentKeywords(userId))
                .build();
        return CustomResponse.onSuccess(SuccessCode.FEED_RECENT_LOAD_SUCCESS, result);
    }

    // 피드 최근 검색어 삭제
    public CustomResponse<Void> deleteRecentKeyword(Long userId, String keyword) {

        searchHistoryService.deleteFeedRecentKeyword(userId, keyword);
        return CustomResponse.onSuccess(SuccessCode.FEED_RECENT_REMOVE_SUCCESS);
    }

    // 피드 최근 검색어 전체 삭제
    public CustomResponse<Void> deleteAllRecentKeywords(Long userId) {

        searchHistoryService.deleteAllFeedRecentKeywords(userId);
        return CustomResponse.onSuccess(SuccessCode.FEED_RECENT_REMOVE_ALL_SUCCESS);
    }
}
