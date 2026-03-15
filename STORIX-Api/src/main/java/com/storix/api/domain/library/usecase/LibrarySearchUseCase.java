package com.storix.api.domain.library.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.library.service.LibraryService;
import com.storix.domain.domains.library.dto.StandardLibraryWorksInfo;
import com.storix.domain.domains.search.dto.RecentResponseDto;
import com.storix.domain.domains.search.service.SearchHistoryService;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@UseCase
@RequiredArgsConstructor
public class LibrarySearchUseCase {

    private final LibraryService libraryService;
    private final SearchHistoryService searchHistoryService;

    // 서재 내 작품 검색
    public CustomResponse<Slice<StandardLibraryWorksInfo>> searchWorks(Long userId, String keyword, Pageable pageable) {

        // 검색어 저장
        if (keyword != null && pageable.getPageNumber() == 0) {
            searchHistoryService.addLibrarySearchLog(userId, keyword);
        }

        // 리뷰한 작품 중 검색어와 관련된 작품 정보 조회
        Slice<StandardLibraryWorksInfo> result = libraryService.searchReviewedWorksInfo(userId, keyword, pageable);

        return CustomResponse.onSuccess(SuccessCode.LIBRARY_SEARCH_SUCCESS, result);
    }

    // 최근 검색어 조회
    public CustomResponse<RecentResponseDto> getRecentKeywords(Long userId) {

        RecentResponseDto result = RecentResponseDto.builder()
                .recentKeywords(searchHistoryService.getLibraryRecentKeywords(userId))
                .build();

        return CustomResponse.onSuccess(SuccessCode.LIBRARY_RECENT_LOAD_SUCCESS, result);
    }

    // 최근 검색어 조회
    public CustomResponse<Void> deleteRecentKeyword(Long userId, String keyword) {

        searchHistoryService.deleteLibraryRecentKeyword(userId, keyword);

        return CustomResponse.onSuccess(SuccessCode.LIBRARY_RECENT_REMOVE_SUCCESS);
    }
}
