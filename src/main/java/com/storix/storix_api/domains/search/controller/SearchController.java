package com.storix.storix_api.domains.search.controller;

import com.storix.storix_api.domains.search.application.usecase.SearchUseCase;
import com.storix.storix_api.domains.search.dto.*;
import com.storix.storix_api.domains.search.service.SearchHistoryService;
import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.domains.works.domain.WorksSortType;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name="검색", description = "검색 관련 API")
public class SearchController {

    private final SearchUseCase searchUseCase;
    private final SearchHistoryService searchHistoryService;

    @GetMapping("/works")
    @Operation(summary = "작품 검색", description = "작품명 검색합니다. 결과값은 무한 스크롤로 구성됩니다.")
    public CustomResponse<SearchResponseWrapperDto<WorksSearchResponseDto>> searchWorks(
            @RequestParam String keyword,
            @AuthenticationPrincipal AuthUserDetails authUser,
            @RequestParam(defaultValue = "NAME") WorksSortType sort,
            @RequestParam(defaultValue = "0") int page
    ) {
        Long userId = (authUser != null) ? authUser.getUserId() : null;

        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                searchUseCase.searchWorks(userId, keyword, pageable)
        );
    }

    @GetMapping("/artists")
    @Operation(summary = "작가 검색", description = "작가명을 검색합니다. 결과값은 무한 스크롤로 구성됩니다.")
    public CustomResponse<SearchResponseWrapperDto<ArtistSearchResponseDto>> searchArtists(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10);

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                searchUseCase.searchArtists(keyword, pageable)
        );
    }

    @GetMapping("/trending")
    @Operation(summary = "급상승 검색어 조회", description = "인기 검색어 1~10위와 순위 변동을 조회합니다.")
    public CustomResponse<TrendingResponseDto> getTrending() {
        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                TrendingResponseDto.builder()
                        .trendingKeywords(searchHistoryService.getTrendingKeywords())
                        .build()
        );
    }

    @GetMapping("/recent")
    @Operation(summary = "최근 검색어 조회")
    public CustomResponse<RecentResponseDto> getRecent(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {

        if (authUser == null) {
            return CustomResponse.onSuccess(
                    SuccessCode.SUCCESS,
                    RecentResponseDto.builder()
                            .recentKeywords(Collections.emptyList())
                            .build()
            );
        }

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                RecentResponseDto.builder()
                        .recentKeywords(searchHistoryService.getRecentKeywords(authUser.getUserId()))
                        .build()
        );
    }

    @DeleteMapping("/recent")
    @Operation(summary = "최근 검색어 삭제")
    public CustomResponse<Void> deleteRecent(
            @RequestParam String keyword,
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {

        if (authUser != null) {
            searchHistoryService.deleteRecentKeyword(authUser.getUserId(), keyword);
        }

        return CustomResponse.onSuccess(SuccessCode.SUCCESS, null);
    }
}
