package com.storix.api.domain.review.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.review.service.WorksDetailReactionService;
import com.storix.domain.domains.review.dto.ReviewLikeToggleResponse;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
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
