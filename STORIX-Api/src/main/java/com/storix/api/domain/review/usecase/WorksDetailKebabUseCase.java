package com.storix.api.domain.review.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.review.service.WorksDetailKebabService;
import com.storix.api.domain.review.controller.dto.ReviewReportRequest;
import com.storix.domain.domains.review.dto.ModifyReviewRequest;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class WorksDetailKebabUseCase {

    private final WorksDetailKebabService worksDetailKebabService;

    // 내 리뷰 수정
    public CustomResponse<Long> modifyMyReview(Long userId, Long reviewId, ModifyReviewRequest req) {

        Long result = worksDetailKebabService.changeReviewDetail(userId, reviewId, req);
        return CustomResponse.onSuccess(SuccessCode.WORKS_DETAIL_REVIEW_UPDATE_SUCCESS, result);
    }

    // 내 리뷰 삭제
    public CustomResponse<Void> removeMyReview(Long userId, Long reviewId) {

        worksDetailKebabService.deleteReview(userId, reviewId);
        return CustomResponse.onSuccess(SuccessCode.WORKS_DETAIL_REVIEW_DELETE_SUCCESS);
    }

    // 리뷰 신고
    public CustomResponse<Void> reportReview(Long userId, Long reviewId, ReviewReportRequest req) {

        worksDetailKebabService.reportReview(userId, reviewId, req.reportedUserId(), req.reason(), req.otherReason());
        return CustomResponse.onSuccess(SuccessCode.WORKS_DETAIL_REVIEW_REPORT_SUCCESS);
    }
}
