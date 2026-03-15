package com.storix.storix_api.domains.library.controller;

import com.storix.storix_api.domains.library.application.usecase.LibrarySearchUseCase;
import com.storix.storix_api.domains.library.application.usecase.LibraryUseCase;
import com.storix.storix_api.domains.library.domain.LibrarySortType;
import com.storix.storix_api.domains.library.dto.LibraryWrapperDto;
import com.storix.storix_api.domains.library.dto.StandardLibraryWorksInfo;
import com.storix.storix_api.domains.search.dto.RecentResponseDto;
import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/library")
@RequiredArgsConstructor
@Tag(name = "서재", description = "서재 관련 API")
public class LibraryController {

    private final LibraryUseCase libraryUseCase;
    private final LibrarySearchUseCase librarySearchUseCase;

    @Operation(summary = "내 리뷰 작품 정보 조회", description = "내가 리뷰한 작품 정보와 리뷰 평점을 조회하는 api 입니다. 무한스크롤 형식입니다.")
    @GetMapping("/review")
    public ResponseEntity<CustomResponse<LibraryWrapperDto<StandardLibraryWorksInfo>>> getReviewedWorksInfo(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam(defaultValue = "LATEST") LibrarySortType sort,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(libraryUseCase.getReviewedWorksInfo(authUserDetails.getUserId(), pageable));
    }

    @Operation(summary = "서재 내 작품 검색", description = "서재 내 작품을 검색하는 api 입니다. 무한스크롤 형식입니다.")
    @GetMapping("/search/works")
    public ResponseEntity<CustomResponse<Slice<StandardLibraryWorksInfo>>> searchLibraryWorks(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10);
        return ResponseEntity.ok()
                .body(librarySearchUseCase.searchWorks(authUserDetails.getUserId(), keyword, pageable));
    }

    @Operation(summary = "최근 검색어 조회")
    @GetMapping("/search/recent")
    public ResponseEntity<CustomResponse<RecentResponseDto>> getRecentSearch(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return ResponseEntity.ok()
                .body(librarySearchUseCase.getRecentKeywords(authUserDetails.getUserId()));
    }

    @Operation(summary = "최근 검색어 삭제")
    @DeleteMapping("/search/recent")
    public ResponseEntity<CustomResponse<Void>> deleteRecentSearch(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok()
                .body(librarySearchUseCase.deleteRecentKeyword(authUserDetails.getUserId(), keyword));
    }

}
