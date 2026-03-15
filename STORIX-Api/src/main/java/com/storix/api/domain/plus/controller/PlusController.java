package com.storix.api.domain.plus.controller;

import com.storix.api.domain.plus.usecase.BoardUseCase;
import com.storix.api.domain.plus.usecase.ReviewUseCase;
import com.storix.api.domain.plus.controller.dto.ArtistBoardUploadRequest;
import com.storix.api.domain.plus.controller.dto.ReaderBoardUploadRequest;
import com.storix.domain.domains.plus.dto.ReaderReviewRedirectResponse;
import com.storix.api.domain.plus.controller.dto.ReaderReviewUploadRequest;
import com.storix.domain.domains.search.application.SearchUseCase;
import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.WorksSearchResponseDto;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.works.domain.WorksPlusSortType;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plus")
@RequiredArgsConstructor
@Tag(name = "[+] 탭", description = "[+] 탭 관련 API")
public class PlusController {

    private final SearchUseCase searchUseCase;
    private final BoardUseCase boardUseCase;
    private final ReviewUseCase reviewUseCase;

    @Operation(summary = "독자 게시물 등록", description = "독자 게시물을 등록하는 api 입니다.   \n이미지를 선택한 직후의 렌더링은 프론트에서 진행해주시고, 이미지를 S3 버킷에 업로드한 후 objectKey와 함께 호출해주세요.")
    @PostMapping("/reader/board")
    public ResponseEntity<CustomResponse<Void>> uploadReaderBoard(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestBody ReaderBoardUploadRequest req
    ) {
        return ResponseEntity.ok()
                .body(boardUseCase.createReaderBoard(authUserDetails.getUserId(), req));
    }

    @Operation(summary = "독자 리뷰 등록", description = "독자 리뷰를 등록하는 api 입니다.")
    @PostMapping("/reader/review")
    public ResponseEntity<CustomResponse<ReaderReviewRedirectResponse>> uploadReaderReview(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestBody ReaderReviewUploadRequest req
    ) {
        return ResponseEntity.ok()
                .body(reviewUseCase.createReaderReview(authUserDetails.getUserId(), req));
    }

    @Operation(summary = "작가 게시물 등록", description = "작가 게시물을 등록하는 api 입니다.   \n이미지를 선택한 직후의 렌더링은 프론트에서 진행해주시고, 이미지를 S3 버킷에 업로드한 후 objectKey와 함께 호출해주세요.")
    @PostMapping("/artist/board")
    public ResponseEntity<CustomResponse<Void>> uploadArtistBoard(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestBody ArtistBoardUploadRequest req
    ) {
        return ResponseEntity.ok()
                .body(boardUseCase.createArtistBoard(authUserDetails.getUserId(), req));
    }

    @GetMapping("/reader/works")
    @Operation(summary = "작품 검색", description = "작품명 검색합니다. 결과값은 무한 스크롤로 구성됩니다.")
    public CustomResponse<PlusSearchResponseWrapperDto<WorksSearchResponseDto>> searchFavoriteWorks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "NAME") WorksPlusSortType sort,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());

        return CustomResponse.onSuccess(
                SuccessCode.PLUS_WORKS_LOAD_SUCCESS,
                searchUseCase.searchWorksForWriting(keyword, pageable)
        );
    }

    @Operation(summary = "리뷰 중복 여부 조회", description = "리뷰 중복 여부를 조회하는 api 입니다.")
    @GetMapping("/reader/review/{worksId}")
    public ResponseEntity<CustomResponse<Void>> checkDuplicateReview(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long worksId
    ) {
        return ResponseEntity.ok()
                .body(reviewUseCase.checkDuplicateReview(authUserDetails.getUserId(), worksId));
    }

}