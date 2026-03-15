package com.storix.storix_api.domains.review.application.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.review.application.service.WorksDetailReactionService;
import com.storix.storix_api.domains.review.dto.ReviewLikeToggleResponse;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class WorksDetailReactionUseCase {

    private final WorksDetailReactionService worksDetailReactionService;

    // 리뷰 좋아요 토글링
    public CustomResponse<ReviewLikeToggleResponse> toggleReviewLike(Long userId, Long reviewId) {

        ReviewLikeToggleResponse result = worksDetailReactionService.toggleReviewLike(userId, reviewId);
        return CustomResponse.onSuccess(SuccessCode. WORKS_DETAIL_REVIEW_LIKE_SUCCESS, result);
    }

}
