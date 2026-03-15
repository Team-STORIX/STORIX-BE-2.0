package com.storix.api.domain.review.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.plus.service.ReviewService;
import com.storix.domain.domains.plus.dto.SliceReviewInfo;
import com.storix.domain.domains.review.dto.DetailedReviewInfoWithProfile;
import com.storix.domain.domains.review.dto.SliceReviewInfoWithProfile;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@UseCase
@RequiredArgsConstructor
public class WorksDetailReviewUseCase {

    private final ReviewService reviewService;

    // 내 리뷰 조회
    public CustomResponse<SliceReviewInfo> getMyReview(AuthUserDetails authUserDetails, Long worksId) {

        Long userId = (authUserDetails != null) ? authUserDetails.getUserId() : null;

        if (userId == null) {
            return CustomResponse.onSuccess(SuccessCode.SUCCESS, null);
        }

        SliceReviewInfo result = reviewService.findMyReview(userId, worksId);
        return CustomResponse.onSuccess(SuccessCode.WORKS_DETAIL_MY_REVIEW_LOAD_SUCCESS, result);
    }

    // 다른 유저 리뷰 전체 조회
    public CustomResponse<Slice<SliceReviewInfoWithProfile>> getOtherReview(AuthUserDetails authUserDetails, Long worksId, Pageable pageable) {

        Long userId = (authUserDetails != null) ? authUserDetails.getUserId() : null;

        Slice<SliceReviewInfoWithProfile> result = reviewService.findAllReviewWithoutMine(userId, worksId, pageable);
        return CustomResponse.onSuccess(SuccessCode.WORKS_DETAIL_OTHER_REVIEW_LOAD_SUCCESS, result);
    }

    // 리뷰 단건 조회
    public CustomResponse<DetailedReviewInfoWithProfile> getReviewDetail(AuthUserDetails authUserDetails, Long reviewId) {

        Long userId = (authUserDetails != null) ? authUserDetails.getUserId() : null;

        DetailedReviewInfoWithProfile result = reviewService.findReviewDetail(userId, reviewId);
        return CustomResponse.onSuccess(SuccessCode.WORKS_DETAIL_REVIEW_DETAIL_LOAD_SUCCESS, result);
    }

}