package com.storix.api.domain.review.controller;

import com.storix.domain.domains.plus.dto.SliceReviewInfo;
import com.storix.api.domain.review.usecase.WorksDetailReactionUseCase;
import com.storix.domain.domains.review.dto.ModifyReviewRequest;
import com.storix.api.domain.review.controller.dto.ReviewReportRequest;
import com.storix.domain.domains.review.domain.ReviewSortType;
import com.storix.domain.domains.review.dto.DetailedReviewInfoWithProfile;
import com.storix.api.domain.review.usecase.WorksDetailKebabUseCase;
import com.storix.api.domain.review.usecase.WorksDetailReviewUseCase;
import com.storix.domain.domains.review.dto.ReviewLikeToggleResponse;
import com.storix.domain.domains.review.dto.SliceReviewInfoWithProfile;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.common.payload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/works")
@RequiredArgsConstructor
@Tag(name = "작품", description = "작품 관련 REST API")
public class ReviewController {

    private final WorksDetailReviewUseCase worksDetailReviewUseCase;
    private final WorksDetailKebabUseCase worksDetailKebabUseCase;
    private final WorksDetailReactionUseCase worksDetailReactionUseCase;

    @Operation(summary = "내 리뷰 조회", description = "작품 id로 내 리뷰를 조회하는 api 입니다. 작성한 리뷰가 없거나 비로그인 유저인 경우, result 필드가 반환되지 않습니다.")
    @GetMapping("/{worksId}/review/me")
    public ResponseEntity<CustomResponse<SliceReviewInfo>> getMyReview(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long worksId
    ) {
        return ResponseEntity.ok()
                .body(worksDetailReviewUseCase.getMyReview(authUserDetails, worksId));
    }

    @Operation(summary = "다른 유저 리뷰 전체 조회", description = "작품 id로 관련된 다른 유저의 리뷰를 전체 조회하는 api 입니다. 무한 스크롤 형식입니다.")
    @GetMapping("/{worksId}/review")
    public ResponseEntity<CustomResponse<Slice<SliceReviewInfoWithProfile>>> getOtherReview(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long worksId,
            @RequestParam(defaultValue = "LATEST") ReviewSortType sort,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(worksDetailReviewUseCase.getOtherReview(authUserDetails, worksId, pageable));
    }

    @Operation(summary = "리뷰 단건 조회", description = "리뷰 id로 리뷰 상세정보를 조회하는 api 입니다.")
    @GetMapping("/review/{reviewId}")
    public ResponseEntity<CustomResponse<DetailedReviewInfoWithProfile>> getReviewDetail(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long reviewId
    ) {
        return ResponseEntity.ok()
                .body(worksDetailReviewUseCase.getReviewDetail(authUserDetails, reviewId));
    }

    @Operation(summary = "리뷰 좋아요", description = "리뷰 id로 좋아요를 토글링하는 api 입니다. 좋아요 여부와 최신 좋아요 수가 반환됩니다.")
    @PostMapping("/review/{reviewId}/like")
    public ResponseEntity<CustomResponse<ReviewLikeToggleResponse>> toggleReviewLike(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok()
                .body(worksDetailReactionUseCase.toggleReviewLike(authUserDetails.getUserId(), reviewId));
    }

    @Operation(summary = "내 리뷰 수정", description = "리뷰 id로 리뷰를 수정하는 api 입니다. 수정된 리뷰 id가 반환됩니다.")
    @PatchMapping("/review/{reviewId}")
    public ResponseEntity<CustomResponse<Long>> modifyMyReview(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long reviewId,
            @Valid @RequestBody ModifyReviewRequest req
    ) {
        return ResponseEntity.ok()
                .body(worksDetailKebabUseCase.modifyMyReview(authUserDetails.getUserId(), reviewId, req));
    }

    @Operation(summary = "내 리뷰 삭제", description = "리뷰 id로 리뷰를 삭제하는 api 입니다.")
    @DeleteMapping("/review/{reviewId}")
    public ResponseEntity<CustomResponse<Void>> removeMyReview(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long reviewId
    ) {
        return ResponseEntity.ok()
                .body(worksDetailKebabUseCase.removeMyReview(authUserDetails.getUserId(), reviewId));
    }

    @Operation(summary = "리뷰 신고", description = "독자 리뷰를 신고하는 api 입니다.")
    @PostMapping("/review/{reviewId}/report")
    public ResponseEntity<CustomResponse<Void>> report(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long reviewId,
            @Valid @RequestBody ReviewReportRequest req
    ) {
        return ResponseEntity.ok()
                .body(worksDetailKebabUseCase.reportReview(authUserDetails.getUserId(), reviewId, req));
    }

}
