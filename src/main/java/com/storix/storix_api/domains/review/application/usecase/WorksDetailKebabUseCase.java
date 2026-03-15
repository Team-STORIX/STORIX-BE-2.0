package com.storix.storix_api.domains.review.application.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.review.application.service.WorksDetailKebabService;
import com.storix.storix_api.domains.review.controller.dto.ModifyReviewRequest;
import com.storix.storix_api.domains.review.controller.dto.ReviewReportRequest;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
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

        worksDetailKebabService.reportReview(userId, reviewId, req);
        return CustomResponse.onSuccess(SuccessCode.WORKS_DETAIL_REVIEW_REPORT_SUCCESS);
    }
}
