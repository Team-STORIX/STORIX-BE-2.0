package com.storix.storix_api.domains.plus.application.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.plus.application.service.ReviewService;
import com.storix.storix_api.domains.plus.controller.dto.ReaderReviewRedirectResponse;
import com.storix.storix_api.domains.plus.controller.dto.ReaderReviewUploadRequest;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ReviewUseCase {

    private final ReviewService reviewService;

    public CustomResponse<ReaderReviewRedirectResponse> createReaderReview(Long userId, ReaderReviewUploadRequest req) {
        ReaderReviewRedirectResponse result = reviewService.createReview(userId, req);
        return CustomResponse.onSuccess(SuccessCode.PLUS_REVIEW_UPLOAD_SUCCESS, result);
    }

    public CustomResponse<Void> checkDuplicateReview(Long userId, Long worksId) {
        reviewService.isReviewExist(userId, worksId);
        return CustomResponse.onSuccess(SuccessCode.PLUS_REVIEW_CHECK_SUCCESS);
    }

}
